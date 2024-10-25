## sbt project compiled with Scala 3

### Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will start a Scala 3 REPL.

For more information on the sbt-dotty plugin, see the
[scala3-example-project](https://github.com/scala/scala3-example-project/blob/main/README.md).


jpackage --input target/scala-3.5.2/ \
         --name MidiTransposerApp \
         --main-jar MidiTransposerApp.jar \
         --main-class Main \
         --type app-image

Contents/Info.plist の中を以下のようにするとmidファイルを推奨アプリケーションとしてくれるっぽい

```
<plist version="1.0">
  <dict>
    ...
    <key>CFBundleDocumentTypes</key>
    <array>
      <dict>
        <key>CFBundleTypeName</key>
        <string>MIDI file</string>
        <key>CFBundleTypeExtensions</key>
        <array>
          <string>mid</string>
          <string>MID</string>
        </array>
        <key>CFBundleTypeIconFile</key>
        <string>AppIcon</string> <!-- 任意、アプリのアイコンを設定 -->
        <key>LSHandlerRank</key>
        <string>Alternate</string> <!-- 推奨アプリケーションとして追加 -->
        <key>CFBundleTypeRole</key>
        <string>Viewer</string> <!-- アプリケーションの役割 -->
      </dict>
    </array>
    ...
  </dict>
</plist>
```