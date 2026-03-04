# Extract the directory path
MODDIR="${0%/*}"

# Start the daemon directly in the background within a private mount namespace
unshare --propagation slave -m "$MODDIR/daemon" --system-server-max-retry=3 "$@" &
