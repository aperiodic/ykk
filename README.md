# ykk

Some utilities for clojure zippers:
- `find-{branch, leaf}`: search a zipper for a loc.
- `filter-{branch, leaf}`: keep certain locs in a zipper.
- `remove-{branch, leaf}`: remove locs from a zipper.
- `map-{branch, leaf}`: transform nodes of a zipper.

## Usage

Leiningen users, stick `[ykk "0.1.0"]` in your dependencies list.
Everybody else, why aren't you using leiningen?

Every function has detailed docstrings, but if you'd like to look at some usage
examples, see `examples/examples.clj`.

## License

Copyright (c) 2012 Dan Lidral-Porter

Distributed under the GNU Affero General Public License (see LICENSE).
