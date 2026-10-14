import java.io.*;
import java.util.*;

public class ExamScheduler {
    static int N, R;
    static boolean[][] graph;
    static int bestExactSlots = Integer.MAX_VALUE;
    static int[] bestExactColors;
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== EXAM TIMETABLE SCHEDULER ===");
        System.out.println("Enter N (courses), M (conflicts), R (max exams per slot):");
        
        if (!sc.hasNextInt()) return;
        N = sc.nextInt();
        int M = sc.nextInt();
        R = sc.nextInt();
        
        graph = new boolean[N][N];
        System.out.println("Enter " + M + " conflicts (u v):");
        for (int i = 0; i < M; i++) {
            int u = sc.nextInt();
            int v = sc.nextInt();
            graph[u][v] = true;
            graph[v][u] = true;
        }

        // Greedy Heuristic
        long startGreedy = System.currentTimeMillis();
        int[] greedyColors = runGreedy();
        long timeGreedy = System.currentTimeMillis() - startGreedy;
        int greedySlots = getSlotCount(greedyColors);

        // Exact Method (Branch and Bound)
        bestExactColors = new int[N];
        bestExactSlots = Integer.MAX_VALUE;
        long startExact = System.currentTimeMillis();
        int[] currentColors = new int[N];
        Arrays.fill(currentColors, -1);
        int[] slotCounts = new int[N];
        
        boolean exactOptimal = true;
        if (N <= 20) {
            solveExact(0, 0, currentColors, slotCounts, greedySlots);
        } else {
            bestExactSlots = greedySlots;
            System.arraycopy(greedyColors, 0, bestExactColors, 0, N);
            exactOptimal = false;
        }
        long timeExact = System.currentTimeMillis() - startExact;

        int[] finalColors = bestExactSlots <= greedySlots ? bestExactColors : greedyColors;
        int finalSlots = bestExactSlots <= greedySlots ? bestExactSlots : greedySlots;

        List<List<Integer>> slots = new ArrayList<>();
        for (int i = 0; i < finalSlots; i++) slots.add(new ArrayList<>());
        for (int i = 0; i < N; i++) slots.get(finalColors[i]).add(i);

        // STRCIT COMPLIANCE: Output 1
        System.out.println("\n--- OUTPUT 1: ALGORITHM RESULTS ---");
        System.out.println("Greedy: " + greedySlots + " slots. Optimal Proven: False");
        System.out.println("Exact:  " + bestExactSlots + " slots. Optimal Proven: " + exactOptimal);
        
        // STRICT COMPLIANCE: Output 2
        System.out.println("\n--- OUTPUT 2: TIMETABLE ---");
        for (int i = 0; i < finalSlots; i++) {
            System.out.println("Slot " + i + ": " + slots.get(i));
        }

        // STRICT COMPLIANCE: Output 3
        writeCSVs(slots, finalColors);
        
        // STRICT COMPLIANCE: Generate data for "As N increases" chart
        runBenchmarkAsNIncreases();
        
        System.out.println("\nAll CSV files (including benchmarks) generated successfully!");
        sc.close();
    }

    private static int[] runGreedy() {
        int[] colors = new int[N];
        Arrays.fill(colors, -1);
        Integer[] nodes = new Integer[N];
        for (int i = 0; i < N; i++) nodes[i] = i;
        Arrays.sort(nodes, (a, b) -> Integer.compare(getDegree(b), getDegree(a)));

        int[] slotCounts = new int[N];
        for (int u : nodes) {
            for (int c = 0; c < N; c++) {
                if (slotCounts[c] < R && isSafe(u, c, colors)) {
                    colors[u] = c;
                    slotCounts[c]++;
                    break;
                }
            }
        }
        return colors;
    }

    private static void solveExact(int u, int maxColorUsed, int[] colors, int[] slotCounts, int maxAllowed) {
        if (maxColorUsed >= bestExactSlots || maxColorUsed >= maxAllowed) return;
        if (u == N) {
            bestExactSlots = maxColorUsed;
            System.arraycopy(colors, 0, bestExactColors, 0, N);
            return;
        }
        for (int c = 0; c < maxColorUsed + 1 && c < bestExactSlots - 1; c++) {
            if (slotCounts[c] < R && isSafe(u, c, colors)) {
                colors[u] = c;
                slotCounts[c]++;
                solveExact(u + 1, Math.max(maxColorUsed, c + 1), colors, slotCounts, maxAllowed);
                colors[u] = -1;
                slotCounts[c]--;
            }
        }
    }

    private static boolean isSafe(int u, int c, int[] colors) {
        for (int v = 0; v < N; v++) {
            if (graph[u][v] && colors[v] == c) return false;
        }
        return true;
    }

    private static int getDegree(int u) {
        int deg = 0;
        for (int v = 0; v < N; v++) if (graph[u][v]) deg++;
        return deg;
    }

    private static int getSlotCount(int[] colors) {
        int max = 0;
        for (int c : colors) max = Math.max(max, c);
        return max + 1;
    }

    private static void writeCSVs(List<List<Integer>> slots, int[] colors) {
        try {
            PrintWriter pwGrid = new PrintWriter(new File("timetable.csv"));
            for (int i = 0; i < slots.size(); i++) {
                pwGrid.print("Slot " + i);
                for (int course : slots.get(i)) pwGrid.print("," + course);
                pwGrid.println();
            }
            pwGrid.close();

            PrintWriter pwGraph = new PrintWriter(new File("graph.csv"));
            pwGraph.println("u,v");
            for (int i = 0; i < N; i++) {
                for (int j = i + 1; j < N; j++) {
                    if (graph[i][j]) pwGraph.println(i + "," + j);
                }
            }
            pwGraph.close();

            PrintWriter pwColors = new PrintWriter(new File("node_colors.csv"));
            pwColors.println("node,slot");
            for (int i = 0; i < N; i++) pwColors.println(i + "," + colors[i]);
            pwColors.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // NEW: Background benchmark to satisfy "as N increases" constraint
    private static void runBenchmarkAsNIncreases() {
        try {
            PrintWriter pw = new PrintWriter(new File("benchmark.csv"));
            pw.println("N,GreedySlots,GreedyTimeMs,ExactSlots,ExactTimeMs");
            
            int[] testSizes = {5, 10, 15, 20}; // Testing as N increases
            for (int testN : testSizes) {
                N = testN;
                R = 3; 
                graph = new boolean[N][N];
                Random rand = new Random(42); 
                for (int i = 0; i < N; i++) {
                    for (int j = i + 1; j < N; j++) {
                        if (rand.nextDouble() < 0.3) {
                            graph[i][j] = true;
                            graph[j][i] = true;
                        }
                    }
                }
                
                // Run Greedy
                long t1 = System.currentTimeMillis();
                int gs = getSlotCount(runGreedy());
                long greedyTime = System.currentTimeMillis() - t1;
                
                // Run Exact
                bestExactSlots = Integer.MAX_VALUE;
                bestExactColors = new int[N];
                int[] colors = new int[N];
                Arrays.fill(colors, -1);
                int[] counts = new int[N];
                
                long t2 = System.currentTimeMillis();
                solveExact(0, 0, colors, counts, gs); // Bounded by greedy
                long exactTime = System.currentTimeMillis() - t2;
                
                pw.println(testN + "," + gs + "," + greedyTime + "," + bestExactSlots + "," + exactTime);
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}