package chess
package format.pgn

import scala.language.implicitConversions

class RoundtripTest extends ChessTest:

  "tags" should:
    "roundtrip with special chars" in:
      val value = "aä\\\"'$%/°á \t\b \"\\\\/"
      Parser.full(Pgn(tags = Tags(List(Tag(_.Site, value))), InitialPosition.empty, None).toString) must beValid.like:
        case parsed =>
          parsed.tags("Site") must_== Some(value)
