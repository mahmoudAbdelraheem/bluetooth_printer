import 'package:bluetooth_printer_example/app_permission.dart';
import 'package:bluetooth_printer_example/bluetooth_dvices_page.dart';
import 'package:flutter/material.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await AppPermission.requestPermissions();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      debugShowCheckedModeBanner: false,
      home: BluetoothDevicesPage(),
    );
  }
}
