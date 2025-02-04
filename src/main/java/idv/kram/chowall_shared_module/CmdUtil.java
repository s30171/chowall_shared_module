package idv.kram.chowall_shared_module;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CmdUtil {

    // 使用 ConcurrentHashMap 儲存執行緒ID與 Process 的對應關係
    private static final Map<Long, Process> threadProcessMap = new ConcurrentHashMap<>();

    public static String exec(String cmd) {
        return exec(cmd.split(" "));
    }

    public static String exec(String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).start();
            // 以當前執行緒 ID 作為 key 儲存對應的 process
            threadProcessMap.put(Thread.currentThread().getId(), p);
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

    // 根據傳入的執行緒ID關閉對應的 Process
    public static void stopProcessByThreadId(long threadId) {
        Process process = threadProcessMap.get(threadId);
        if (process != null && process.isAlive()) {
            log.info("Stopping process for thread id: {}", threadId);
            process.destroy(); // 或使用 process.destroyForcibly() 強制關閉
            threadProcessMap.remove(threadId);
        } else {
            log.info("No active process found for thread id: {}", threadId);
        }
    }

    public static void stopAllProcess() {
        threadProcessMap.forEach((threadId, process) -> {
            if (process.isAlive()) {
                log.info("Stopping process for thread id: {}", threadId);
                process.destroy();
            }
        });
        threadProcessMap.clear();
    }
}
