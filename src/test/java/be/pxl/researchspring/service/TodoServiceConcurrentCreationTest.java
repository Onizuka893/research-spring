package be.pxl.researchspring.service;

import be.pxl.researchspring.domain.Todo;
import be.pxl.researchspring.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class TodoServiceConcurrentCreationTest {
    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    private static final int CONCURRENT_USERS = 100;
    private static final int TODOS_PER_USER = 100;
    private static final int EXPECTED_TOTAL = CONCURRENT_USERS * TODOS_PER_USER;

    @BeforeEach
    public void setUp() {
        todoRepository.deleteAll();
    }

    @Test
    public void testConcurrentTodoCreation() throws InterruptedException, ExecutionException {
        System.out.println("Starting concurrent todo creation test...");
        System.out.println("Users: " + CONCURRENT_USERS);
        System.out.println("Todos per user: " + TODOS_PER_USER);
        System.out.println("Expected total: " + EXPECTED_TOTAL);
        System.out.println("----------------------------------------");

        // Performance metrics
        long startTime = System.currentTimeMillis();
        AtomicInteger successfulCreations = new AtomicInteger(0);
        AtomicInteger failedCreations = new AtomicInteger(0);

        // Thread pool to simulate concurrent users
        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_USERS);

        // Synchronization primitives
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(CONCURRENT_USERS);

        // Store futures to get results
        List<Future<UserResult>> futures = new ArrayList<>();

        // Create and submit tasks for each user
        for (int userId = 1; userId <= CONCURRENT_USERS; userId++) {
            final int userNumber = userId;

            Future<UserResult> future = executorService.submit(() -> {
                UserResult result = new UserResult(userNumber);

                try {
                    // Wait for start signal so all threads start simultaneously
                    startSignal.await();
                    System.out.println("User " + userNumber + " started creating todos...");

                    // Create todos for this user
                    for (int todoNumber = 1; todoNumber <= TODOS_PER_USER; todoNumber++) {
                        try {
                            String title = String.format("User_%d_Todo_%d", userNumber, todoNumber);
                            Todo created = todoService.create(title);

                            result.createdTodos.add(created);
                            successfulCreations.incrementAndGet();

                            // Log progress every 25 todos
                            if (todoNumber % 25 == 0) {
                                System.out.println(String.format("User %d: %d/%d todos created",
                                        userNumber, todoNumber, TODOS_PER_USER));
                            }

                        } catch (Exception e) {
                            result.errors.add(e.getMessage());
                            failedCreations.incrementAndGet();
                            System.err.println("User " + userNumber + " failed to create todo: " + e.getMessage());
                        }
                    }

                    System.out.println("User " + userNumber + " completed!");

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    result.errors.add("User interrupted: " + e.getMessage());
                } finally {
                    doneSignal.countDown();
                }

                return result;
            });

            futures.add(future);
        }

        // Start all threads at the same time
        System.out.println("\nStarting all users simultaneously...\n");
        startSignal.countDown();

        // Wait for all users to complete (with timeout)
        boolean completed = doneSignal.await(2, TimeUnit.MINUTES);
        assertTrue(completed, "Test did not complete within 2 minutes timeout");

        // Shutdown executor service
        executorService.shutdown();

        // Collect and analyze results
        System.out.println("\n=== RESULTS ===");
        for (Future<UserResult> future : futures) {
            UserResult result = future.get();
            System.out.println(String.format("User %d: Created %d todos, Errors: %d",
                    result.userId, result.createdTodos.size(), result.errors.size()));
        }

        // Calculate metrics
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Verify results
        long actualTodosInDb = todoRepository.count();

        System.out.println("\n=== FINAL METRICS ===");
        System.out.println("Total execution time: " + duration + " ms");
        System.out.println("Successful creations: " + successfulCreations.get());
        System.out.println("Failed creations: " + failedCreations.get());
        System.out.println("Actual todos in database: " + actualTodosInDb);
        System.out.println("Average time per todo: " +
                String.format("%.2f ms", (double) duration / successfulCreations.get()));
        System.out.println("Throughput: " +
                String.format("%.2f todos/second", successfulCreations.get() * 1000.0 / duration));

        // Assertions
        assertEquals(EXPECTED_TOTAL, successfulCreations.get(),
                "All todos should be created successfully");
        assertEquals(0, failedCreations.get(),
                "There should be no failed creations");
        assertEquals(EXPECTED_TOTAL, actualTodosInDb,
                "Database should contain all created todos");

        // Verify no duplicates by checking unique titles
        List<String> titles = todoRepository.findAll().stream()
                .map(Todo::getTitle)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        assertEquals(EXPECTED_TOTAL, titles.size(), "All todo titles should be unique");
    }

    @Test
    public void testConcurrentTodoCreationAsync() throws InterruptedException {
        System.out.println("\nStarting ASYNC concurrent todo creation test...");
        System.out.println("Users: " + CONCURRENT_USERS);
        System.out.println("Todos per user: " + TODOS_PER_USER);
        System.out.println("Expected total: " + EXPECTED_TOTAL);
        System.out.println("----------------------------------------");

        long startTime = System.currentTimeMillis();
        CountDownLatch completionLatch = new CountDownLatch(EXPECTED_TOTAL);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        List<CompletableFuture<Todo>> allFutures = new ArrayList<>();

        // Create all async operations
        for (int userId = 1; userId <= CONCURRENT_USERS; userId++) {
            for (int todoNum = 1; todoNum <= TODOS_PER_USER; todoNum++) {
                String title = String.format("AsyncUser_%d_Todo_%d", userId, todoNum);

                CompletableFuture<Todo> future = todoService.createAsync(title)
                        .whenComplete((todo, throwable) -> {
                            if (throwable == null) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                                System.err.println("Async error: " + throwable.getMessage());
                            }
                            completionLatch.countDown();
                        });

                allFutures.add(future);
            }
        }

        // Wait for all operations to complete
        boolean completed = completionLatch.await(2, TimeUnit.MINUTES);
        assertTrue(completed, "Async test did not complete within timeout");

        // Wait for all futures to complete
        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Verify results
        long actualCount = todoRepository.count();

        System.out.println("\n=== ASYNC RESULTS ===");
        System.out.println("Total execution time: " + duration + " ms");
        System.out.println("Successful creations: " + successCount.get());
        System.out.println("Failed creations: " + errorCount.get());
        System.out.println("Actual todos in database: " + actualCount);
        System.out.println("Throughput: " +
                String.format("%.2f todos/second", successCount.get() * 1000.0 / duration));

        assertEquals(EXPECTED_TOTAL, successCount.get(),
                "All async todos should be created successfully");
        assertEquals(EXPECTED_TOTAL, actualCount,
                "Database should contain all created todos");
    }

    // Helper class to store results for each user
    private static class UserResult {
        final int userId;
        final List<Todo> createdTodos = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        UserResult(int userId) {
            this.userId = userId;
        }
    }
}
