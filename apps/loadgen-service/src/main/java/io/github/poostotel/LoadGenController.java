package io.github.poostotel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RestController
public class LoadGenController {

    // Baseline
    @GetMapping("/idle")
    public String idle() {
        return "OK";
    }

    // CPU-bound
    @GetMapping("/cpu")
    public String cpu(@RequestParam(defaultValue = "10000") int iterations) {
        long sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += Math.sqrt(Math.pow(i, 2) + Math.sin(i));
        }
        return "CPU work done: " + sum;
    }

    // Memory-bound
    @GetMapping("/memory")
    public String memory(@RequestParam(defaultValue = "100") int mb) {
        int size = mb * 1024 * 1024 / 8;
        List<double[]> chunks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            chunks.add(new double[size / 10]);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Allocated ~" + mb + " MB";
    }

    // Mixed
    @GetMapping("/mixed")
    public String mixed(
            @RequestParam(defaultValue = "5000") int cpuIters,
            @RequestParam(defaultValue = "50") int memMb
    ) {
        cpu(cpuIters);
        memory(memMb);
        return "Mixed load completed";
    }

    // Cold Start simulation
    private static volatile boolean coldStartDone = false;

    @GetMapping("/cold-start")
    public String coldStart() throws InterruptedException {
        if (!coldStartDone) {
            Thread.sleep(3000); // имитация инициализации
            coldStartDone = true;
        }
        return "Cold start simulated";
    }

    // Network + CPU
    @GetMapping("/network-cpu")
    public String networkCpu(
            @RequestParam(defaultValue = "1000") int cpuIters,
            @RequestParam(defaultValue = "https://httpbin.org/delay/1") String url
    ) throws IOException, InterruptedException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(5000);
        int status = conn.getResponseCode();
        conn.disconnect();

        long sum = 0;
        for (int i = 0; i < cpuIters * 1000; i++) {
            sum += Math.log(Math.abs(Math.sin(i) + 1e-10));
        }

        return "Network status: " + status + ", CPU sum: " + sum;
    }
}