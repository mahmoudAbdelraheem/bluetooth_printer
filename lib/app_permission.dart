import 'package:permission_handler/permission_handler.dart';

class AppPermission {
  static Future<void> requestPermissions() async {
    // Request necessary permissions for Android 11+
    if (await Permission.bluetoothScan.request().isGranted &&
        await Permission.bluetoothConnect.request().isGranted &&
        await Permission.locationWhenInUse.request().isGranted) {
      print("All permissions granted");
    } else {
      print("Permissions not granted");
    }
  }
}
