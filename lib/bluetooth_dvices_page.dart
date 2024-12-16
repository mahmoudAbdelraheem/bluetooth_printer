import 'package:bluetooth_printer_example/bluetooth_services.dart';
import 'package:flutter/material.dart';
import 'package:screenshot/screenshot.dart';

class BluetoothDevicesPage extends StatefulWidget {
  const BluetoothDevicesPage({super.key});

  @override
  _BluetoothDevicesPageState createState() => _BluetoothDevicesPageState();
}

class _BluetoothDevicesPageState extends State<BluetoothDevicesPage> {
  final BluetoothServices bluetoothServices = BluetoothServices();
  List<String> scanResult = [];
  ScreenshotController imageController = ScreenshotController();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Bluetooth Devices")),
      body: Column(
        children: [
          ElevatedButton(
            onPressed: () async {
              scanResult = await bluetoothServices.scanDevices();
              setState(() {}); // إعادة تحميل الـ List بعد المسح
            },
            child: const Text("Scan for Devices"),
          ),
          ElevatedButton(
            onPressed: () async {
              if (bluetoothServices.selectedDeviceAddress.isNotEmpty) {
                // التقاط صورة الفاتورة وتحويلها إلى Byte Array للطباعة
                await bluetoothServices.printImage(imageController);
              } else {
                print("No printer connected");
              }
            },
            child: const Text("Print Image"),
          ),
          ElevatedButton(
            onPressed: () async {
              if (bluetoothServices.selectedDeviceAddress.isNotEmpty) {
                // التقاط صورة الفاتورة وتحويلها إلى Byte Array للطباعة
                await bluetoothServices.printText('\nhello form محمود\n\n');
              } else {
                print("No printer connected");
              }
            },
            child: const Text("Print text"),
          ), // my invoce
          Container(
            child: Screenshot(
              controller: imageController,
              child: Container(
                margin: const EdgeInsets.all(15),
                padding: const EdgeInsets.all(10),
                width: double.infinity,
                height: 250,
                decoration: BoxDecoration(
                  border: Border.all(color: Colors.black, width: 1),
                  borderRadius: BorderRadius.circular(20),
                  color: Colors.white,
                ),
                child: Column(
                  children: [
                    Image.asset(
                      'images/flutter.png',
                      width: 130,
                      height: 130,
                      fit: BoxFit.contain,
                    ),
                    const Text('invoice sn:32321'),
                    const Text('camera man: محمود'),
                    const Text('phone: +01012012451'),
                    const Text('price: 30\$'),
                  ],
                ),
              ),
            ),
          ),
          Expanded(
            child: ListView.builder(
              itemCount: scanResult.length,
              itemBuilder: (context, index) {
                return InkWell(
                  onTap: () async {
                    print(
                        '${scanResult[index].split('\n')[0]} is selected with mac address = ${scanResult[index].split('\n')[1]}');
                    await bluetoothServices
                        .connectToPrinter(scanResult[index].split('\n')[1]);
                  },
                  child: ListTile(
                    title: Text(scanResult[index].split('\n')[0]),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
