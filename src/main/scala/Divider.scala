package chess

import cats.syntax.all.*
import bitboard.Bitboard
import scala.annotation.switch

case class Division(middle: Option[Ply], end: Option[Ply], plies: Ply):

  def openingSize: Ply        = middle | plies
  def middleSize: Option[Ply] = middle.map((end | plies) - _)
  def endSize                 = end.map(plies - _)
  def openingBounds           = middle.map(0 -> _)
  def middleBounds            = (middle, end).tupled
  def endBounds               = end.map(_ -> plies)

object Division:
  val empty = Division(None, None, Ply.initial)

object Divider:

  def apply(boards: List[Board]): Division =

    val indexedBoards: List[(Board, Int)] = boards.zipWithIndex

    val midGame = indexedBoards.foldLeft(none[Int]):
      case (None, (board, index)) =>
        (majorsAndMinors(board) <= 10 ||
          backrankSparse(board) ||
          mixedness(board) > 150) option index
      case (found, _) => found

    val endGame =
      if midGame.isDefined then
        indexedBoards.foldLeft(none[Int]):
          case (found: Some[?], _) => found
          case (_, (board, index)) => (majorsAndMinors(board) <= 6) option index
      else None

    Division(
      Ply from midGame.filter(m => endGame.fold(true)(m < _)),
      Ply from endGame,
      Ply(boards.size)
    )

  private def majorsAndMinors(board: Board): Int =
    (board.occupied & ~(board.kings | board.pawns)).count

  // Sparse back-rank indicates that pieces have been developed
  private def backrankSparse(board: Board): Boolean =
    (Bitboard.firstRank & board.white).count < 4 ||
      (Bitboard.lastRank & board.black).count < 4

  private def score(white: Int, black: Int, y: Int): Int =
    ((white, black): @switch) match
      case (0, 0) => 0

      case (1, 0) => 1 + (8 - y)
      case (2, 0) => if y > 2 then 2 + (y - 2) else 0
      case (3, 0) => if y > 1 then 3 + (y - 1) else 0
      case (4, 0) =>
        if y > 1 then 3 + (y - 1) else 0 // group of 4 on the homerow = 0

      case (0, 1) => 1 + y
      case (1, 1) => 5 + (3 - y).abs
      case (2, 1) => 4 + y
      case (3, 1) => 5 + y

      case (0, 2) => if y < 6 then 2 + (6 - y) else 0
      case (1, 2) => 4 + (6 - y)
      case (2, 2) => 7

      case (0, 3) => if y < 7 then 3 + (7 - y) else 0
      case (1, 3) => 5 + (6 - y)

      case (0, 4) => if y < 7 then 3 + (7 - y) else 0

      case _ => 0

  private val mixednessRegions: List[List[Square]] = {
    for
      y <- Rank.all.take(7)
      x <- File.all.take(7)
    yield {
      for
        dy   <- 0 to 1
        dx   <- 0 to 1
        file <- x.offset(dx)
        rank <- y.offset(dy)
      yield Square(file, rank)
    }.toList
  }.toList

  private def mixedness(board: Board): Int =
    mixednessRegions.foldLeft(0): (mix, region) =>
      var white = 0
      var black = 0
      region.foreach: s =>
        board(s).foreach: v =>
          if v is White then white = white + 1
          else black = black + 1
      mix + score(white, black, region.head.rank.index + 1)
