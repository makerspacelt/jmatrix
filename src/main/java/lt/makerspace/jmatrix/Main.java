package lt.makerspace.jmatrix;

import com.googlecode.lanterna.TerminalSize;
import lt.makerspace.jmatrix.cli.TerminalSizeConverter;
import lt.makerspace.jmatrix.textupdater.*;
import lt.makerspace.jmatrix.textupdater.FileText.ImageSize;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main implements Callable<Integer> {

    @Option(names = "--text", negatable = true, defaultValue = "true", description = "Naudoti teksto išjungimui")
    private boolean text;

    @Option(names = {"--showTimePerFrame", "-T"}, defaultValue = "false")
    private boolean showTimePerFrame;

    @Parameters(index = "0", arity = "0..1", description = """
                Pagal prioritetą:
                filePath - rodomas failo turinys arba juodai baltas paveiklėlis
                clock - rodomas paprastas laikrodis
                chucknorris - testavimui, faktai apie chuck norris facts
                kitas tekstas - rodomas nesikeičiantis tekstas
        """)
    private String display;

    @Option(names = "--imageSize", defaultValue = "FULL")
    private ImageSize imageSize;

    @Option(names = {"--softCap", "-s"}, defaultValue = "500")
    private int softCap;

    @Option(names = {"--hardCap", "-h"}, defaultValue = "2000")
    private int hardCap;

    @Option(names = {"--matrixSize", "-S"}, converter = TerminalSizeConverter.class, description = """
        Kokio dydžio turėtų būti matrica. Naudoti jei nepavyksta nustatyti automatiškai.
        """)
    private TerminalSize sizeOverride;

    @Option(names = {"--tty"}, description = """
        Failas arba tcp url (host:port) kuris bus naudojamas kaip terminalas
        """)
    private String tty;

    @Override
    public Integer call() throws Exception {

        ScheduledExecutorService ses = Executors.newScheduledThreadPool(3);

        if (display == null || display.isBlank()) {
            text = false;
        }


        InputStream in = System.in;
        OutputStream out = System.out;

        if (StringUtils.isNotBlank(tty)) {
            File file = new File(tty);
            if (file.exists()) {
                in = new FileInputStream(file);
                out = new FileOutputStream(file);
            } else {
                String[] split = tty.split(":");
                Socket socket = new Socket(split[0], Integer.parseInt(split[1]));
                in = socket.getInputStream();
                out = socket.getOutputStream();
            }
        }

        Matrix matrix;
        if (!text) {
            matrix = new Matrix(ses, null, in, out);
        } else {
            TextUpdater updater;
            try {
                Path path = Path.of(display);
                updater = new FileText(path, imageSize, ses);
            } catch (Exception e) {
                updater = switch (display) {
                    case "clock" -> new LocalDateTimeText(ses);
                    case "chucknorris" -> new ChuckNorrisText(ses);
                    default -> new ConstantText(display);
                };
            }
            matrix = new Matrix(ses, updater, in, out);
        }
        matrix.setShowUpdateTime(showTimePerFrame);
        matrix.setSoftCap(softCap);
        matrix.setHardCap(hardCap);
        if (sizeOverride != null) {
            matrix.setSizeOverride(sizeOverride);
        }

        matrix.start();
        while (!matrix.isExited()) {
            Thread.sleep(10000);
        }

        return 0;
    }

    public static void main(String[] args) throws Exception {
        int exitVal = new CommandLine(new Main()).execute(args);
        System.exit(exitVal);
    }
}
