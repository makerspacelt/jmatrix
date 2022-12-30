Gauti terminalo kuris bus naudojamas dydį

    stty --file=/dev/tty1 size

Grąžina formatu `eilutės stulpeliai`, performatuoti į `StulpeliaixEilutės`,
pvz jei gauta `120 135`, suformatuoti į `135x120`

Tada galima renderinti į tty kitam kompe su:

    java -Xmx16m -jar target/jmatrix.jar -S 135x120 "Mano Tekstas" qrcode.png | ssh -C pi@tv-matrix "cat > /dev/tty1"