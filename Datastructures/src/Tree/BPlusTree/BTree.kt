package Tree.BPlusTree

/*
class BTree<T extends Comparable> {
  final int _degree;

  int _length = 0;

  _Node<T> _root;

  /// Returns `true` if the tree has no items in it.
  bool get isEmpty => _length == 0;

  /// Returns `true` if the tree has items in it.
  bool get isNotEmpty => _length != 0;

  /// The number of items currently in the tree.
  int get length => _length;

  /// The max number of items to allow per node.
  int get _maxItems => _degree * 2 - 1;

  /// The min number of items to allow per node (ignored for the root node).
  int get _minItems => _degree - 1;

  /// Creates a new B-tree with the given degree.
  ///
  /// `new BTree(2)`, for example, will create a 2-3-4 tree
  /// (each node contains 1-3 items and 2-4 children).
  BTree(this._degree);

  /// Returns `true` if the given key is in the tree.
  bool contains(T key) => this[key] != null;
*/
class BTree<T : Comparable<T>>
()
{

}
/*
  /// Adds the given item to the tree. If an item in the tree already equals
  /// the given one, it is removed from the tree and returned.
  ///
  /// Otherwise, `null` is returned.
  ///
  /// Adding `null` to the tree throws an [ArgumentError].
  T replaceOrInsert(T item) {
    checkNotNull(item);
    if (_root == null) {
      _root = new _Node(this);
      _root.items.add(item);
      _length++;
      return null;
    } else if (_root.items.length >= _maxItems) {
      final result = _root.split(_maxItems ~/ 2);
      final item2 = result.item1;
      final second = result.item2;
      final oldRoot = _root;
      _root = new _Node(this)
        ..items.add(item2)
        ..children.addAll([oldRoot, second]);
    }
    T out = _root.insert(item, _maxItems);
    if (out == null) {
      _length++;
    }
    return out;
  }

  /// Removes an item equal to the passed in [item] from the tree, returning it.
  /// If no such item exists, returns `null`.
  T remove(T item) => _removeItem(item, _RemoveType.removeItem);

  /// Removes the smallest item in the tree and returns it. If no such item
  /// exists, returns `null`.
  T removeMin() => _removeItem(null, _RemoveType.removeMin);

  /// Removes the largest item in the tree and returns it. If no such item
  /// exists, returns `null`.
  T removeMax() => _removeItem(null, _RemoveType.removeMax);

  T _removeItem(T item, _RemoveType type) {
    if (_root == null || _root.items.isEmpty) {
      return null;
    }
    final out = _root.remove(item, _minItems, type);
    if (_root.items.isEmpty && _root.children.isNotEmpty) {
      _root = _root.children.first;
    }
    if (out != null) {
      _length--;
    }
    return out;
  }

  /// Looks for the key item in the tree, returning it. It returns `null` if
  /// unable to find that item.
  T operator [](T key) {
    if (_root == null) return null;
    return _root[key];
  }

  /// Calls the iterator for every value in the tree within the range
  /// [greaterOrEqual, lessThan), until iterator returns false.
  void ascendRange(T greaterOrEqual, T lessThan, ItemIterator<T> iterator) {
    if (_root == null) return;
    _root.iterate((T a) => a.compareTo(greaterOrEqual) >= 0,
        (T a) => a.compareTo(lessThan) < 0, iterator);
  }

  /// Calls the iterator for every value in the tree within the range
  /// [first, pivot), until iterator returns false.
  void ascendLessThan(T pivot, ItemIterator<T> iterator) {
    if (_root == null) return;
    _root.iterate((T a) => true, (T a) => a.compareTo(pivot) < 0, iterator);
  }

  /// Calls the iterator for every value in the tree within
  /// the range [pivot, last], until iterator returns false.
  void ascendGreaterOrEqual(T pivot, ItemIterator<T> iterator) {
    if (_root == null) return;
    _root.iterate((T a) => a.compareTo(pivot) >= 0, (T a) => true, iterator);
  }

  /// Calls the iterator for every value in the tree within the range
  /// [first, last], until iterator returns false.
  void ascend(ItemIterator<T> iterator) {
    if (_root == null) return;
    _root.iterate((T a) => true, (T a) => true, iterator);
  }
}

 */