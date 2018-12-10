# Getting Started

1. Install [NetBeans IDE 8.0.2](https://netbeans.org/downloads/8.0.2/). It is important to use exactly version 8.0.2.

1. Clone feat:

        git clone https://github.com/adrianeboyd/feat.git

1. Start netbeans and open the projects `Czesl` and `nb_modules` from the `feat` directory.

1. In netbeans, build the `nb_modules` project. (Right-click and select `Build` or use other toolbar/menu options to build.)

1. In netbeans, run the `feat` project. (Right-click and select `Run` or use other toolbar/menu options to run.) This will launch the feat tool.

1. To package feat for distribution, run `ant build-zips` from the `Czesl` directory:

        cd feat/Czesl
        ant build-zips

    Alternatively, right-click on `feat` in netbeans and select `Package as -> Zip Distribution`.

    The zip files will be in `feat/Czesl/dist`. The file `feat.zip` includes everything an annotator needs to run feat on multiple operating systems. The annotator should unpack `feat.zip` and run `feat/bin/feat` or `feat/bin/feat.exe` to start feat.

