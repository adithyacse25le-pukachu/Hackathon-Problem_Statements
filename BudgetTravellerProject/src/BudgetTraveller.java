import java.io.*;
import java.util.*;

public class BudgetTraveller {

    static class Edge {
        int to;
        int price;

        public Edge(int to, int price) {
            this.to = to;
            this.price = price;
        }
    }

    static class State implements Comparable<State> {
        int cost, city, stops, coupons;
        List<Integer> path;
        List<Boolean> couponFlags;

        public State(int cost, int city, int stops, int coupons, List<Integer> path, List<Boolean> couponFlags) {
            this.cost = cost;
            this.city = city;
            this.stops = stops;
            this.coupons = coupons;
            this.path = new ArrayList<>(path);
            this.couponFlags = new ArrayList<>(couponFlags);
        }

        @Override
        public int compareTo(State other) {
            return Integer.compare(this.cost, other.cost);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== BUDGET TRAVELLER SOLVER ===");
        System.out.println("Enter N (cities), M (flights), K (max stops), C (coupons) on a single line:");
        
        if (!scanner.hasNextInt()) return;
        int N = scanner.nextInt();
        int M = scanner.nextInt();
        int K = scanner.nextInt();
        int C = scanner.nextInt();

        List<List<Edge>> graph = new ArrayList<>();
        for (int i = 0; i <= N; i++) {
            graph.add(new ArrayList<>());
        }

        System.out.println("Enter " + M + " flights (each on a new line as: u v p):");
        for (int i = 0; i < M; i++) {
            int u = scanner.nextInt();
            int v = scanner.nextInt();
            int p = scanner.nextInt();
            graph.get(u).add(new Edge(v, p));
        }

        solveAndOutput(N, K, C, graph);
        scanner.close();
    }

    public static void solveAndOutput(int N, int K, int C, List<List<Edge>> graph) {
        PriorityQueue<State> pq = new PriorityQueue<>();
        
        int[][][] minCostTable = new int[N + 1][K + 2][C + 1];
        for (int i = 0; i <= N; i++) {
            for (int j = 0; j <= K + 1; j++) {
                Arrays.fill(minCostTable[i][j], Integer.MAX_VALUE);
            }
        }

        List<Integer> startPath = new ArrayList<>();
        startPath.add(1);
        List<Boolean> startCoupons = new ArrayList<>();

        pq.add(new State(0, 1, 0, 0, startPath, startCoupons));
        minCostTable[1][0][0] = 0;

        State bestFinalState = null;

        while (!pq.isEmpty()) {
            State curr = pq.poll();

            if (curr.city == N) {
                bestFinalState = curr;
                break;
            }

            if (curr.cost > minCostTable[curr.city][curr.stops][curr.coupons]) {
                continue;
            }

            for (Edge edge : graph.get(curr.city)) {
                int nextStops = curr.stops + 1;
                if (nextStops > K + 1) continue; 

                // Option A: Without Coupon
                int costWithoutCoupon = curr.cost + edge.price;
                if (costWithoutCoupon < minCostTable[edge.to][nextStops][curr.coupons]) {
                    minCostTable[edge.to][nextStops][curr.coupons] = costWithoutCoupon;
                    List<Integer> nextPath = new ArrayList<>(curr.path);
                    nextPath.add(edge.to);
                    List<Boolean> nextCouponFlags = new ArrayList<>(curr.couponFlags);
                    nextCouponFlags.add(false);
                    pq.add(new State(costWithoutCoupon, edge.to, nextStops, curr.coupons, nextPath, nextCouponFlags));
                }

                // Option B: With Coupon
                if (curr.coupons < C) {
                    int discountedPrice = edge.price / 2;
                    int costWithCoupon = curr.cost + discountedPrice;
                    int nextCoupons = curr.coupons + 1;
                    if (costWithCoupon < minCostTable[edge.to][nextStops][nextCoupons]) {
                        minCostTable[edge.to][nextStops][nextCoupons] = costWithCoupon;
                        List<Integer> nextPath = new ArrayList<>(curr.path);
                        nextPath.add(edge.to);
                        List<Boolean> nextCouponFlags = new ArrayList<>(curr.couponFlags);
                        nextCouponFlags.add(true);
                        pq.add(new State(costWithCoupon, edge.to, nextStops, nextCoupons, nextPath, nextCouponFlags));
                    }
                }
            }
        }

        if (bestFinalState == null) {
            System.out.println("Output 1: -1");
        } else {
            System.out.println("\n--- RESULTS ---");
            System.out.println("Output 1: " + bestFinalState.cost);
            printAndSaveOutput2(bestFinalState, graph);
        }
    }

    public static void printAndSaveOutput2(State finalState, List<List<Edge>> graph) {
        System.out.println("Path taken: " + finalState.path);
        
        System.out.println("Flight Coupon Breakdown:");
        for (int i = 0; i < finalState.couponFlags.size(); i++) {
            int from = finalState.path.get(i);
            int to = finalState.path.get(i + 1);
            boolean used = finalState.couponFlags.get(i);
            
            int price = 0;
            for (Edge e : graph.get(from)) {
                if (e.to == to) {
                    price = e.price;
                    break;
                }
            }

            if (used) {
                System.out.println("Flight " + from + " -> " + to + " | Coupon USED | Price: " + price + " -> " + (price / 2));
            } else {
                System.out.println("Flight " + from + " -> " + to + " | No Coupon | Price: " + price);
            }
        }
    }
}