#!/usr/bin/env bash
# Publish pilo-backend, pilo-frontend, and pilo-worker to separate remotes with history (git subtree split).
# Development stays in this monorepo; run this script before delivery or after meaningful releases.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

ENV_FILE="${SUBTREE_REMOTES_ENV:-$ROOT/scripts/subtree-remotes.env}"
DRY_RUN=0
for arg in "$@"; do
	case "$arg" in
		--dry-run) DRY_RUN=1 ;;
		-h | --help)
			cat <<'EOF'
Usage: ./scripts/publish-subtree-repos.sh [--dry-run]

Reads scripts/subtree-remotes.env (see subtree-remotes.example.env).
Creates/updates local branches publish/backend|frontend|worker via git subtree split,
then pushes each to the configured remote on PILO_PUBLISH_BRANCH.

Commit or stash local changes before publishing.
EOF
			exit 0
			;;
	esac
done

if [[ ! -f "$ENV_FILE" ]]; then
	echo "Missing $ENV_FILE — copy scripts/subtree-remotes.example.env and set repo URLs." >&2
	exit 1
fi

# shellcheck source=/dev/null
source "$ENV_FILE"

: "${PILO_BACKEND_REMOTE:?}"
: "${PILO_FRONTEND_REMOTE:?}"
: "${PILO_WORKER_REMOTE:?}"
PILO_PUBLISH_BRANCH="${PILO_PUBLISH_BRANCH:-main}"

if ! git rev-parse --git-dir >/dev/null 2>&1; then
	echo "Not a git repository." >&2
	exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
	echo "Working tree is dirty. Commit or stash before publishing." >&2
	git status -sb
	exit 1
fi

# Remotes are registered by stable names (not URLs as remote names)
ensure_remote() {
	local name="$1"
	local url="$2"
	if git remote get-url "$name" >/dev/null 2>&1; then
		current="$(git remote get-url "$name")"
		if [[ "$current" != "$url" ]]; then
			echo "Updating remote $name -> $url"
			git remote set-url "$name" "$url"
		fi
	else
		git remote add "$name" "$url"
	fi
}

split_and_push() {
	local prefix="$1"
	local remote_name="$2"
	local url="$3"
	local publish_branch="$4"

	local split_branch="publish/${remote_name}"

	echo "==> Split $prefix"
	if [[ "$DRY_RUN" -eq 1 ]]; then
		echo "(dry-run) git subtree split --prefix=$prefix -b $split_branch"
		echo "(dry-run) git push $remote_name $split_branch:$publish_branch"
		return 0
	fi

	git branch -D "$split_branch" 2>/dev/null || true
	git subtree split --prefix="$prefix" -b "$split_branch"
	git push "$remote_name" "${split_branch}:${publish_branch}"
	echo "    Pushed $remote_name ($url) branch $publish_branch"
}

ensure_remote pilo-backend "$PILO_BACKEND_REMOTE"
ensure_remote pilo-frontend "$PILO_FRONTEND_REMOTE"
ensure_remote pilo-worker "$PILO_WORKER_REMOTE"

split_and_push pilo-backend pilo-backend "$PILO_BACKEND_REMOTE" "$PILO_PUBLISH_BRANCH"
split_and_push pilo-frontend pilo-frontend "$PILO_FRONTEND_REMOTE" "$PILO_PUBLISH_BRANCH"
split_and_push pilo-worker pilo-worker "$PILO_WORKER_REMOTE" "$PILO_PUBLISH_BRANCH"

echo "Done. Monorepo unchanged; child repos updated."
