<head>
    <script src="https://hm.baidu.com/hm.js?8a3608795648935457c4799145ab9d75" async="async"></script>
    <script src="https://cdn.jsdelivr.net/gh/xjl12/count@4/count" async="async"></script>
</head>

# 专为 Redmi 10X 5G 打造的 LineageOS

***

<iframe src="https://ghbtns.com/github-btn.html?user=xjl12&repo=android_device_xiaomi_atom&type=star&count=true&size=large" frameborder="0" scrolling="0" width="200" height="50" title="GitHub"></iframe>

### 感谢您对本项目的持续关注，请先点亮上方的“⭐ Star”按钮，谢谢！

## 基础版（Free）功能支持
- 📞： VoLTE语音通话，短信收发，WLAN，蓝牙文件传输
- 👍：屏幕指纹识别，解锁，登录，支付均支持
- 📷：相机拍照，录像等正常
- 🪪 ：部分金融级软件人脸识别认证不再提示设备环境异常

## 高级版（Plus）独有功能
- 🔓：息屏指纹解锁
- 📳：振动功能修复
- ⚡：充电温度控制
- 🔈：通话界面（微信、腾讯会议）音量可以调节
- 🖼：内置Lawnchair桌面
- 🔐：同步最新Android 8月补丁

## 获取方式
基础版请从[Github Release](https://github.com/xjl12/android_device_xiaomi_atom/releases)中的Assets处免费获取
高级版下载链接（限时仅需 ¥3）：https://www.123pan.cn/ps/Nx6HTd-261D3
文件命名说明：Flashable开头的为在Recovery中使用的经典卡刷包，DSU开头的为动态系统更新包。
SHA256校验值：
```
9da72bcebcdec8d772df50bc269a93b069638881118471771a46f3b96973af20  DSU-lineage-21.0-20240810-free-atom.zip
b7decb1bb77e79bb4d4b34e6d0813bba3539689e2497e7d864eff694f7999a54  Flashable-lineage-21.0-20240810-free-atom.zip
```

## 其他说明
您的自愿付费行为将视为对本项目的捐赠，不支持退款。建议您先下载基础版体验，确保上述基础功能正常后再捐赠支持
推荐使用的系统底包：MIUI V13.0.6 正式版及以上
推荐使用的Recovery：https://github.com/HuaZoffice/OFRP-device_xiaomi_atom/releases 或 https://github.com/ymdzq/OFRP-device_xiaomi_bomb/releases
推荐使用的数据备份软件：https://github.com/XayahSuSuSu/Android-DataBackup/releases
推荐使用的DSU安转辅助软件：https://github.com/VegaBobo/DSU-Sideloader/releases
关于Redmi 10X Pro的支持：建议使用DSU包进行体验，若功能正常，可直接在Recovery中刷入，但刷入后需要再补刷该设备原始的boot.img

## 已知问题
- 蓝牙音频（请使用有线耳机替代）

## 感谢 🙌
@LineageOS @PixelExperience @LawnchairLauncher @phhusson @ymdzq @HuaZoffice @HuaLiMao-AQ @xiaomi-mt6885-devs @MiCode 以及为本项目做出贡献的其他commiters，各位捐赠者们！

如您有付费定制其它类原生系统的需求，请通过Email：xiao-xjle@qq.com洽谈。
有关本ROM的安装、功能问题请通过Issues反馈，大家一起讨论解决。

您可以通过捐赠获取高级版或直接扫描下方二维码对本项目进行支持，感谢您的付出！😘
![赞助方式](/android_device_xiaomi_atom/skm.webp)

## MIUI DSU包制作方法

1. 下载MIUI卡刷包，从中解压出system.new.dat.br、system.transfer.list、product.new.dat.br、product.transfer.list
2. 运行以下命令
```
brotli -d system.new.dat.br
brotli -d product.new.dat.br
python sdat2img.py system.transfer.list system.new.dat system.img
python sdat2img.py product.transfer.list product.new.dat product.img
zip dsu.zip system.img product.img
```

## 感谢 🙌
@LineageOS @PixelExperience @LawnchairLauncher @phhusson @ymdzq @HuaZoffice @HuaLiMao-AQ @xiaomi-mt6885-devs @MiCode 以及为本项目做出贡献的其他commiters，各位捐赠者们！

## 郑重声明
**刷机有风险，由此造成的损失与作者无关。本项目为社区开源项目，未组建任何QQ、微信、Telegram群聊，系统发布及讨论渠道为[GitHub](https://github.com/xjl12/android_device_xiaomi_atom)**
如您有付费定制其它类原生系统的需求，请通过Email：xiao-xjle@qq.com洽谈。
有关本ROM的安装、功能问题请通过[Issues](https://github.com/xjl12/android_device_xiaomi_atom/issues)反馈，由大家一起讨论解决。
