# Contributing

We are open to, and grateful for, any contributions made by the community. By contributing to Paladin Cloud, you agree
to abide by the [code of conduct](https://github.com/PaladinCloud/CE/blob/master/code_of_conduct.md).

## Reporting Issues and Asking Questions

Before opening an issue, please search the [issue tracker](https://github.com/PaladinCloud/CE/issues) to make sure your
issue hasn't already been reported.

### Bugs and Improvements

We use the issue tracker to keep track of bugs and improvements, its examples, and the documentation. We encourage you
to open issues to discuss improvements, architecture, theory, internal implementation, etc. If a topic has been
discussed before, we will ask you to join the previous discussion.

## Semantic Versioning

We follow semantic versioning. We release patch versions for bugfixes, minor versions for new features or non-essential
changes, and major versions for any breaking changes.

## Branch Organization

Submit all changes directly to the **master** branch. We don’t use separate branches for development or for upcoming
releases. We do our best to keep **master** in good shape, with all tests passing.

Code that lands in **master** must be compatible with the latest stable release. It may contain additional features, but
no breaking changes. We should be able to release a new minor version from the tip of main at any time.

## Development

Visit the [issue tracker](https://github.com/PaladinCloud/CE/issues) to find a list of open issues that need attention.

### Sending a Pull Request

For non-trivial changes, please open an issue with a proposal for a new feature or refactoring before starting on the
work. We don't want you to waste your efforts on a pull request that we won't want to accept.

On the other hand, sometimes the best way to start a conversation _is_ to send a pull request. Use your best judgement!

In general, the contribution workflow looks like this:

- Open a new issue in the [Issue tracker](https://github.com/PaladinCloud/CE/issues).
- Fork the repo.
- Create a new feature branch based off the `master` branch.
- Make sure to add tests.
- Submit a pull request, referencing any issues it addresses.

Please try to keep your pull request focused in scope and avoid including unrelated commits.
We follow below guidelines for pull requests and commits:

* We use conventional commits and pull requests.
* some examples of commits:
    * `fix:` for bugfixes
    * `feat:` for new features
    * `docs:` for documentation
    * `style:` for style changes
    * `refactor(auth-api):` for code refactoring
    * `test:` for tests
    * `chore:` for chores
    * `BREAKING CHANGE:` for breaking changes
* Use the same conventions for pull requests as well.

After you have submitted your pull request, we'll try to get back to you as soon as possible. We may suggest some
changes or improvements.

Thank you for contributing!