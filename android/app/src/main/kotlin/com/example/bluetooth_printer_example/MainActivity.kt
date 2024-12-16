package com.example.bluetooth_printer_example

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class MainActivity : FlutterActivity() {
    private val CHANNEL = "bluetooth_printer"
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var connectedPrinterSocket: BluetoothSocket? = null

    companion object {
        private const val PRINTER_WIDTH = 765 // Standard thermal printer width
        private const val PERMISSIONS_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for Bluetooth support
        if (bluetoothAdapter == null) {
            // showToast("Bluetooth is not supported on this device")
            return
        }

        // Request Bluetooth permissions for modern devices
        checkAndRequestBluetoothPermissions()

        // Set up the MethodChannel for communication with Flutter
        setupMethodChannel()
    }

    private fun checkAndRequestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val requiredPermissions = arrayOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN
            )

            val missingPermissions = requiredPermissions.filter { permission ->
                ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
            }

            if (missingPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    missingPermissions.toTypedArray(),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    private fun setupMethodChannel() {
        MethodChannel(flutterEngine!!.dartExecutor, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "scanDevices" -> result.success(scanDevices())
                
                "connectToPrinter" -> {
                    val printerAddress = call.argument<String>("printerAddress")
                    if (printerAddress != null) {
                        handlePrinterConnection(printerAddress, result)
                    } else {
                        result.error("INVALID_ARGUMENT", "No printer address provided", null)
                    }
                }

                "printText" -> {
                    call.argument<String>("text")?.let { text ->
                        printText(text, result)
                    } ?: result.error("INVALID_ARGUMENT", "No text provided", null)
                }

                "printImage" -> {
                    call.argument<ByteArray>("imageBytes")?.let { imageBytes ->
                        printImage(imageBytes, result)
                    } ?: result.error("INVALID_ARGUMENT", "No image provided", null)
                }

                else -> result.notImplemented()
            }
        }
    }

    private fun handlePrinterConnection(printerAddress: String, result: MethodChannel.Result) {
        try {
            // Close any existing connection
            connectedPrinterSocket?.close()
            
            val connectionResult = connectToPrinter(printerAddress)
            if (connectionResult) {
                result.success("Connected to printer")
            } else {
                result.error("CONNECTION_FAILED", "Failed to connect to the printer", null)
            }
        } catch (e: Exception) {
            result.error("CONNECTION_ERROR", "Error during connection: ${e.message}", null)
        }
    }

    private fun scanDevices(): List<String> = 
        bluetoothAdapter?.bondedDevices?.map { device -> 
            "${device.name}\n${device.address}" 
        } ?: emptyList()

    private fun connectToPrinter(printerAddress: String): Boolean {
        return try {
            val device = bluetoothAdapter?.getRemoteDevice(printerAddress) 
                ?: throw IllegalArgumentException("Device not found")
            
            bluetoothAdapter?.cancelDiscovery()

            // Try multiple UUIDs for better compatibility
            val uuids = listOf(
                "00001101-0000-1000-8000-00805F9B34FB", // Standard SerialPortServiceClass UUID
                device.uuids?.firstOrNull()?.uuid?.toString() ?: UUID.randomUUID().toString()
            )

            var socket: BluetoothSocket? = null
            for (uuidString in uuids) {
                try {
                    val uuid = UUID.fromString(uuidString)
                    socket = device.createRfcommSocketToServiceRecord(uuid)
                    socket.connect()
                    break
                } catch (e: IOException) {
                    socket?.close()
                    continue
                }
            }

            connectedPrinterSocket = socket
            socket != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun printImage(imageData: ByteArray, result: MethodChannel.Result) {
        try {
            val outputStream: OutputStream = connectedPrinterSocket?.outputStream 
                ?: throw IOException("Printer not connected")

            // Decode and process the image
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                ?: throw IllegalArgumentException("Invalid image data")

            // Resize and convert bitmap
            val processedBitmap = processImageForPrinter(bitmap)
            val escPosBytes = convertBitmapToEscPosBytes(processedBitmap)

            // Print commands
            val initPrinter = byteArrayOf(0x1B, 0x40)  // Initialize printer
            val lineSpacing = byteArrayOf(0x1B, 0x33, 0x00)  // Line spacing
            
            outputStream.write(initPrinter)
            outputStream.write(lineSpacing)
            outputStream.write(escPosBytes)
            outputStream.write(byteArrayOf(0x0A, 0x0A))  // Line feeds
            outputStream.flush()

            result.success("Image printed successfully")
        } catch (e: Exception) {
            result.error("PRINT_FAILED", "Failed to print image: ${e.message}", null)
        }
    }

    // private fun processImageForPrinter(bitmap: Bitmap): Bitmap {
    //     // Resize image to printer width while maintaining aspect ratio
    //     val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
    //     val newHeight = (PRINTER_WIDTH / aspectRatio).toInt() // Adjust height based on new width
        
    //     val resizedBitmap = Bitmap.createScaledBitmap(bitmap, PRINTER_WIDTH, newHeight, true)
    //     return convertToBlackAndWhite(resizedBitmap)
    // }

    private fun processImageForPrinter(bitmap: Bitmap): Bitmap {
        // Calculate the exact printer width in pixels
        val PRINTER_WIDTH = 72 * 8  // 72mm * 8 dots/mm = 576 pixels
    
        // Resize image to printer width while maintaining aspect ratio
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val newHeight = (PRINTER_WIDTH / aspectRatio).toInt()
        
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, PRINTER_WIDTH, newHeight, true)
        return convertToBlackAndWhite(resizedBitmap)
    }

    
    

    private fun convertToBlackAndWhite(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val blackAndWhiteBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val redValue = Color.red(pixel)
                val greenValue = Color.green(pixel)
                val blueValue = Color.blue(pixel)
                
                // Convert to grayscale
                val grayValue = (0.299 * redValue + 0.587 * greenValue + 0.114 * blueValue).toInt()
                
                // Simple thresholding
                val bwColor = if (grayValue > 128) Color.WHITE else Color.BLACK
                
                blackAndWhiteBitmap.setPixel(x, y, bwColor)
            }
        }
        
        return blackAndWhiteBitmap
    }


   
    

    private fun convertBitmapToEscPosBytes(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
    val height = bitmap.height
    val byteWidth = (width + 7) / 8  // Correct byte width calculation
        
        val outputStream = ByteArrayOutputStream()
        
        // ESC/POS raster image print command
    outputStream.write(byteArrayOf(
        0x1D, // GS
        0x76, // v
        0x30, // 0
        0x00, // Normal mode
        (byteWidth and 0xFF).toByte(), // Width LSB
        ((byteWidth shr 8) and 0xFF).toByte(), // Width MSB
        (height and 0xFF).toByte(), // Height LSB
        ((height shr 8) and 0xFF).toByte() // Height MSB
    ))

        // Convert bitmap to bytes
        for (y in 0 until height) {
            for (x in 0 until byteWidth) {
                var b = 0
                for (bit in 0..7) {
                    val pixelX = x * 8 + bit
                    if (pixelX < width) {
                        b = b shl 1
                        b = b or if (bitmap.getPixel(pixelX, y) == Color.BLACK) 1 else 0
                    }
                }
                outputStream.write(b)
            }
        }

        return outputStream.toByteArray()
    }

    private fun printText(text: String, result: MethodChannel.Result) {
        try {
            val outputStream: OutputStream = connectedPrinterSocket?.outputStream 
                ?: throw IOException("Printer not connected")

            // ESC/POS text printing commands
            val initPrinter = byteArrayOf(0x1B, 0x40)  // Initialize
            val textBytes = text.toByteArray(Charsets.UTF_8)
            val lineFeed = byteArrayOf(0x0A)  // New line

            outputStream.write(initPrinter)
            outputStream.write(textBytes)
            outputStream.write(lineFeed)
            outputStream.flush()

            result.success("Text printed successfully")
        } catch (e: Exception) {
            result.error("PRINT_FAILED", "Failed to print text: ${e.message}", null)
        }
    }

    // private fun showToast(message: String) {
    //     // You can implement this method to show toast messages
    //     // For example: Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    // }
}