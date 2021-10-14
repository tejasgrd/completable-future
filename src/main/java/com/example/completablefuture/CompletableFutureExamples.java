package com.example.completablefuture;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CompletableFutureExamples {

  static Random random = new Random();

  //Creating a Completed CompletableFuture
  static void completedFutureExample(){
    CompletableFuture<String> cf = CompletableFuture.completedFuture("message");
    assertTrue(cf.isDone());
    assertEquals("message",cf.getNow(null));
  }

  //Running a Simple Asynchronous Stage
  //By default (when no Executor is specified), asynchronous execution uses the common ForkJoinPool implementation, which uses daemon threads to execute the
  // Runnable task. Note that this is specific to CompletableFuture. Other CompletionStage implementations can override the default behavior.
  //runAsync takes Runnable as input parameter and returns CompletableFuture<Void>, which means it does not return any result.
  //suppyAsync takes Supplier as argument and returns the CompletableFuture<U> with result value, which means it does not take any input parameters 
  //but it returns result as output.
  static void runAsyncExample(){
    CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
      assertTrue(Thread.currentThread().isDaemon());
      randomSleep();
    });
    assertFalse(cf.isDone());
    sleepEnough();
    assertTrue(cf.isDone());
  }


  //Applying a Function on the Previous Stage
  //then, which means that the action of this stage happens when the current stage completes normally (without an exception). In this case,
  // the current stage is already completed with the value “message”.
  //Apply, which means the returned stage will apply a Function on the result of the previous stage.
  static void thenApplyExample(){
    CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApply(s ->{
      assertFalse(Thread.currentThread().isDaemon());
      return s.toUpperCase();
    });

    assertEquals("MESSAGE",cf.getNow(null));
  }


  //Asynchronously Applying a Function on a Previous Stage
  //By appending the Async suffix to the method in the previous example,
  // the chained CompletableFuture would execute asynchronously (using ForkJoinPool.commonPool()).
  static void thenApplyAsyncExample(){
    CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
      assertTrue(Thread.currentThread().isDaemon());
      randomSleep();
      return s.toUpperCase();
    });
    assertNull(cf.getNow(null));
    assertEquals("MESSAGE",cf.join());
  }
  //Applying a Function to the Result of Either of Two Completed Stages
  //The below example creates a CompletableFuture that applies a Function to the result of either of two previous
  // stages (no guarantees on which one will be passed to the Function). The two stages in question are: one that
  // applies an uppercase conversion to the original string and another that applies a lowercase conversion:
  static void applyToEitherExample() {
    String original = "Message";
    CompletableFuture<String> cf1 = CompletableFuture.completedFuture(original)
        .thenApplyAsync(s -> delayedUpperCase(s));
    CompletableFuture<String> cf2 = cf1.applyToEither(
        CompletableFuture.completedFuture(original).thenApplyAsync(s -> delayedLowerCase(s)),
        s -> s + " from applyToEither");
    assertTrue(cf2.join().endsWith(" from applyToEither"));

  }

  //Accepting the Results of Both Stages in a BiConsumer
  //Instead of executing a Runnable upon completion of both stages, using BiConsumer allows processing of their
  // results if needed:
  static void thenAcceptBothExample(){
    String original = "Message";
    StringBuilder result = new StringBuilder();
    CompletableFuture.completedFuture(original).thenApply(String::toUpperCase).thenAcceptBoth(
        CompletableFuture.completedFuture(original).thenApply(String::toLowerCase),
        (s1, s2) -> result.append(s1 + s2));
    assertEquals("MESSAGEmessage",result.toString());

  }
  // Applying a BiFunction on Results of Both Stages
  //If the dependent CompletableFuture is intended to combine the results of two previous CompletableFutures by
  // applying a function on them and returning a result, we can use the method thenCombine(). The entire pipeline is
  // synchronous, so getNow() at the end would retrieve the final result, which is the concatenation of the uppercase
  // and the lowercase outcomes.
  static void thenCombineExample() {
    String original = "Message";
    CompletableFuture<String> cf = CompletableFuture.completedFuture(original).thenApply(s -> delayedUpperCase(s))
        .thenCombine(CompletableFuture.completedFuture(original).thenApply(s -> delayedLowerCase(s)),
            (s1,s2) -> s1 + s2);

    assertEquals("MESSAGEmessage", cf.getNow(null));
  }


  //Asynchronously Applying a BiFunction on Results of Both Stages
  //Similar to the previous example, but with a different behavior: since the two stages upon which CompletableFuture
  // depends both run asynchronously, the thenCombine() method executes asynchronously, even though it lacks the
  // Async suffix. This is documented in the class Javadocs: “Actions 
  ied for dependent completions of non-async
  // methods may be performed by the thread that completes the current CompletableFuture, or by any other caller of a
  // completion method.” Therefore, we need to join() on the combining CompletableFuture to wait for the result.
  static void thenCombineAsyncExample() {
    String original = "Message";
    CompletableFuture<String> cf = CompletableFuture.completedFuture(original)
        .thenApplyAsync(s -> delayedUpperCase(s))
        .thenCombineAsync(CompletableFuture.completedFuture(original).thenApplyAsync(s -> delayedLowerCase(s)),
            (s1, s2) -> s1 + s2);
    assertEquals("MESSAGEmessage", cf.join());
  }



  static void thenComposeExample() {
    String original = "Message";
    CompletableFuture<String> cf = CompletableFuture.completedFuture(original).thenApply(s -> delayedUpperCase(s))
        .thenCompose(upper -> CompletableFuture.completedFuture(original).thenApply(s -> delayedLowerCase(s))
            .thenApply(s -> upper + s));
    assertEquals("MESSAGEmessage", cf.join());
  }


  static void anyOfExample() {
    StringBuilder result = new StringBuilder();
    List<String> messages = Arrays.asList("a", "b", "c");
    List<CompletableFuture<String>> futures = messages.stream()
        .map(msg -> CompletableFuture.completedFuture(msg).thenApply(s -> delayedUpperCase(s)))
        .collect(Collectors.toList());
    CompletableFuture.anyOf(futures.toArray(new CompletableFuture[futures.size()])).whenComplete((res, th) -> {
      if(th == null) {
        assertTrue(isUpperCase((String) res));
        result.append(res);
      }
    });
    assertTrue("Result was empty", result.length() > 0);
  }


  public static void main(String[] args) {
    completedFutureExample();
    runAsyncExample();
    thenApplyExample();
    applyToEitherExample();
    thenAcceptBothExample();
    thenCombineExample();
  }

  private static String delayedUpperCase(String s) {
    randomSleep();
    return s.toUpperCase();
  }

  private static String delayedLowerCase(String s) {
    randomSleep();
    return s.toLowerCase();
  }


  private static void randomSleep() {
    try {
      Thread.sleep(random.nextInt(1000));
    } catch (InterruptedException e) {
      // ...
    }
  }

  private static void sleepEnough() {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      // ...
    }
  }

  private static boolean isUpperCase(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (Character.isLowerCase(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}

