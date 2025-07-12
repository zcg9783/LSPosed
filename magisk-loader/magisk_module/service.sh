#
# This file is part of LSPosed.
#
# LSPosed is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# LSPosed is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with LSPosed.  If not, see <https://www.gnu.org/licenses/>.
#
# Copyright (C) 2021 LSPosed Contributors
#

MODDIR=${0%/*}

cd "$MODDIR"

# To avoid breaking Play Integrity in certain cases, we start LSPosed service daemon in late_start service mode instead of post-fs-data mode
unshare --propagation slave -m sh -c "$MODDIR/daemon $@&"
SDK_VERSION=$(getprop ro.build.version.sdk)
if [ "$SDK_VERSION" -le 28 ]; then
    resetprop --delete dalvik.vm.dex2oat-flags
fi
