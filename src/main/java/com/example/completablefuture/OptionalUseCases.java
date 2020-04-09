package com.example.completablefuture;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class OptionalUseCases {


  public static void orElseExaemple(){
    String name = Optional.of("Test-1").orElse("Other");
    assertEquals(name,"Test-1");
  }

  public static void orElseWithStreamExample(){
    Optional<String> op = Optional.of(getStringStream().findFirst()).orElse(Optional.of("Absent"));
    assertEquals("fxdghkgghj",op.get());
  }



  public static Stream<String> getStringStream(){
    return Arrays.asList("fxdghkgghj","abiggyvscm","mhmipbukzh","ucxekceyzw","ljqxwxfacv","syudhnqbze","cepjillhia","dxpyqabnij","qszvlllmiv","wtqsldelci").stream();
  }

  public static void main(String[] args) {
    orElseExaemple();
    orElseWithStreamExample();
  }
}
