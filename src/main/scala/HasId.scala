package chess

trait HasId[A, Id]:
  def getId(a: A): Id
  extension (a: A)
    inline def id: Id                    = getId(a)
    inline def sameId(other: A): Boolean = a.id == other.id
    inline def hasId(id: Id): Boolean    = a.id == id

// trait for merge two values of the same type
// A may not sastify Semigroup laws, hence new Mergeable Trait
trait Mergeable[A]:
  def merge(a1: A, a2: A): A
  extension (a1: A) def merge(a2: A)(using mergeable: Mergeable[A]): A = mergeable.merge(a1, a2)
