package benchmarks

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

import cats.syntax.all.*
import chess.format.pgn.{ Fixtures, Parser, Pgn, PgnStr }
import chess.format.pgn.PgnHelper.*

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 15, timeUnit = TimeUnit.SECONDS, time = 3)
@Warmup(iterations = 15, timeUnit = TimeUnit.SECONDS, time = 3)
@Fork(value = 3)
@Threads(value = 1)
class PgnBench:

  // the unit of CPU work per iteration
  private[this] val Work: Long = 10

  var pgnStrs: List[PgnStr] = _
  var pgns: List[Pgn]       = _

  @Setup
  def setup() =
    pgnStrs = Fixtures.gamesForPerfTest
    pgns = pgnStrs.traverse(Parser.full).toOption.get.map(_.toPgn)

  @Benchmark
  def pgnParser(bh: Blackhole) =
    var result = pgnStrs.map: x =>
      Blackhole.consumeCPU(Work)
      Parser.full(x)
    bh.consume(result)
    result

  @Benchmark
  def pgnRender(bh: Blackhole) =
    var result = pgns.map: x =>
      Blackhole.consumeCPU(Work)
      x.render
    bh.consume(result)
    result
