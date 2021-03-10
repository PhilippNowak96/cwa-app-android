package de.rki.coronawarnapp.test.eventregistration.ui.qrcode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.graphics.pdf.PdfDocument
import android.view.View
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class QrCodeCreationTestViewModel @AssistedInject constructor(
    private val dispatcher: DispatcherProvider,
    private val fileSharing: FileSharing,
    @AppContext private val context: Context
) : CWAViewModel(dispatcher) {

    val qrCodeBitmap = SingleLiveEvent<Bitmap>()
    val errorMessage = SingleLiveEvent<String>()
    val sharingIntent = SingleLiveEvent<FileSharing.ShareIntentProvider>()

    /**
     * Creates a QR Code [Bitmap] ,result is delivered by [qrCodeBitmap]
     */
    fun createQrCode(input: String) = launch(context = dispatcher.IO) {
        qrCodeBitmap.postValue(encodeAsBitmap(input))
    }

    /**
     * Create a new PDF file and result is delivered by [sharingIntent]
     * as a sharing [FileSharing.ShareIntentProvider]
     */
    fun createPDF(
        view: View
    ) = launch(context = dispatcher.IO) {
        try {
            val file = pdfFile()
            val pageInfo = PdfDocument.PageInfo.Builder(
                view.width,
                view.height,
                1
            ).create()

            PdfDocument().apply {
                startPage(pageInfo).apply {
                    view.draw(canvas)
                    finishPage(this)
                }

                FileOutputStream(file).use {
                    writeTo(it)
                    close()
                }
            }

            sharingIntent.postValue(
                fileSharing.getIntentProvider(file, "Scan and Help")
            )
        } catch (e: Exception) {
            errorMessage.postValue(e.localizedMessage ?: "Creating pdf failed")
            Timber.d(e, "Creating pdf failed")
        }
    }

    private fun pdfFile(): File {
        val dir = File(context.filesDir, "events")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "CoronaWarnApp-Event.pdf")
    }

    private fun encodeAsBitmap(input: String, size: Int = 1000): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.CHARACTER_SET to Charsets.UTF_8
            )
            MultiFormatWriter().encode(
                input,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            ).toBitmap()
        } catch (e: Exception) {
            Timber.d(e, "Qr code creation failed")
            errorMessage.postValue(e.localizedMessage ?: "QR code creation failed")
            null
        }
    }

    private fun BitMatrix.toBitmap() =
        Bitmap.createBitmap(
            context.resources.displayMetrics,
            width,
            height,
            Bitmap.Config.ARGB_8888
        ).apply {
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val color = if (get(x, y)) BLACK else WHITE
                    setPixel(x, y, color)
                }
            }
        }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<QrCodeCreationTestViewModel>
}
