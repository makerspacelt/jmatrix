package lt.makerspace.jmatrix;

import lt.makerspace.jmatrix.textupdater.*;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

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

    @Override
    public Integer call() throws Exception {

        ScheduledExecutorService ses = Executors.newScheduledThreadPool(3);

        if (display == null || display.isBlank()) {
            text = false;
        }

        Matrix matrix;
        if (!text) {
            matrix = new Matrix(ses, null);
        } else {
            TextUpdater updater;
            try {
                Path path = Path.of(display);
                updater = new FileText(path, ses);
            } catch (Exception e) {
                updater = switch (display) {
                    case "clock" -> new LocalDateTimeText(ses);
                    case "chucknorris" -> new ChuckNorrisText(ses);
                    default -> new ConstantText(display);
                };
            }
            matrix = new Matrix(ses, updater);
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
