package lt.makerspace.jmatrix.cli;

import com.googlecode.lanterna.TerminalSize;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.ITypeConverter;

public class TerminalSizeConverter implements ITypeConverter<TerminalSize> {
    @Override
    public TerminalSize convert(String s) throws Exception {
        if (StringUtils.isBlank(s)) {
            return null;
        }

        String[] split = s.split("x");
        if (split.length != 2) {
            split = s.split(" ");
        }
        if (split.length != 2) {
            throw new IllegalArgumentException("Terminal size should be COLSxROWS");
        }

        int cols = Integer.parseInt(split[0]);
        int rows = Integer.parseInt(split[1]);

        return new TerminalSize(cols, rows);
    }
}
