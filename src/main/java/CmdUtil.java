import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

@Slf4j
public class CmdUtil {
    public static String exec(String cmd) {
        return exec(cmd.split(" "));
    }

    public static String exec(String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).start();
            p.info().command().ifPresent(log::info);
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                    stderr.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                throw e;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()));
                 ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                    stdout.append(line).append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            int exitVal = p.waitFor();
            String std;
            String cmdStr = String.join(" ", cmd);
            if (exitVal == 0) {
                std = stdout.toString();
                log.info("[cmd success]: {}", cmdStr);
            } else {
                std = stderr.toString();
                log.error("[cmd fail]: {}", cmdStr);
            }
            p.destroy();
            return std;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
