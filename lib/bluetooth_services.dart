import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:screenshot/screenshot.dart';

class BluetoothServices {
  int maxPrinterWidth = 576;
  final platform = const MethodChannel('bluetooth_printer');
  String selectedDeviceAddress = '';

  // فحص الأجهزة المتوفرة
  Future<List<String>> scanDevices() async {
    List<String> devices = [];
    // التأكد من الأذونات المطلوبة
    if (await Permission.bluetoothScan.isGranted &&
        await Permission.bluetoothConnect.isGranted) {
      try {
        // استدعاء الكود النيتف لمسح الأجهزة
        final result = await platform.invokeMethod('scanDevices');
        List<String> scannedDevices = List<String>.from(result);
        devices = scannedDevices;
        print("Devices found: $devices");
      } on PlatformException catch (e) {
        print("Failed to scan devices: ${e.message}");
      }
    } else {
      print("Permissions are not granted");
      await Permission.bluetoothScan.request();
      await Permission.bluetoothConnect
          .request(); // طلب الأذونات إذا لم تكن موجودة
    }
    return devices;
  }

  // الاتصال بالطابعة
  Future<void> connectToPrinter(String printerAddress) async {
    try {
      final result = await platform.invokeMethod('connectToPrinter', {
        'printerAddress': printerAddress,
      });
      print(result); // "Connected to printer"
      selectedDeviceAddress = printerAddress;
    } on PlatformException catch (e) {
      print("Failed to connect to printer: ${e.message}");
    }
  }

  Future<void> printImage(ScreenshotController imageController) async {
    try {
      Uint8List? imageBytes = await imageController.capture(
          pixelRatio: 3.0, delay: const Duration(milliseconds: 50));

      if (imageBytes == null) {
        print('Failed to capture image');
        return;
      }

      try {
        await platform.invokeMethod('printImage', {'imageBytes': imageBytes});
      } on PlatformException catch (e) {
        print('Printing error: ${e.message}');
        // Optionally implement retry logic here
      }
    } catch (e) {
      print('Image capture error: $e');
    }
  }

  // Print text
  Future<void> printText(String text) async {
    try {
      if (text.isEmpty) {
        print("No text to print");
        return;
      }

      await platform.invokeMethod('printText', {
        'text': text,
      });
      print("Text sent to printer");
    } on PlatformException catch (e) {
      print("Failed to print text: ${e.message}");
    }
  }
}
