# Air purifier Controller
 - [Introductions](#introdution)
 - [Requirements](#requirements)
 - [Installation](#installation)
  ## Introductions
 Project được thực hiện nhằm mục đích 
 - Điều khiển thiết bị máy lọc không khí từ xa qua app điện thoại
 - Điều khiển tự động thiết bị dựa trên chất lượng không khí trong phòng
 
 Với phần server, sử dụng nodejs server để thực hiện điều khiển, giám sát thông tin về chất lượng không khí trong phòng, trạng thái hiện tại của thiết bị thông qua các thiết bị broadlink
 
 Phần app sử dụng bản tin MQTT để  gửi lệnh điều khiển thiết bị cho người dùng.
 
  ## Requirements

- [Nodejs](https://nodejs.org/en/) > 8 
- MQTT Aws Iot hoặc ([mosquitto](https://mosquitto.org/)
- Thiết bị Broadlink sử dụng để điều khiển và theo dõi công suất e.g. RM 3 mini 3, SmartPlug
- Điện thoại Android
- Thiết bị máy lọc không khí

## Installation
- Server
```sh

npm install 
```
 
 
