# classes.dex provenance

Built on 2026-11-02 from the two sources in this directory with the Android build tools'
d8, release mode, no desugaring needed. With the sources copied to `src/dexy/app/Dexy.java` and
`src/dexy/lib/LibDex.java`, these commands rebuild the committed file byte for byte:

    javac --release 11 -d classes src/dexy/app/Dexy.java src/dexy/lib/LibDex.java
    d8 --release --output out classes/dexy/app/Dexy.class classes/dexy/lib/LibDex.class

(jdk 17.0.19-tem, build-tools 36.0.0). The dex carries the two classes only; no
AndroidManifest.xml, so `atom callsites -l dex` needs no android.jar and the input names
no boundary itself.
