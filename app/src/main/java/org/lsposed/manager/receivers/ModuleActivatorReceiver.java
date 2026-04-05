package org.lsposed.manager.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import org.lsposed.manager.ConfigManager;
import org.lsposed.manager.adapters.ScopeAdapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ModuleActivatorReceiver extends BroadcastReceiver {

    private static final String ACTION_ACTIVATE = "com.zcg.ACTIVATE_MODULE";
    private static final String EXTRA_PKG_NAME = "pkg_name";
    private static final String EXTRA_SCOPES = "scopes";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            activateDefaultModule(context);
        } else if (action.equals(ACTION_ACTIVATE)) {
            String pkgName = intent.getStringExtra(EXTRA_PKG_NAME);
            String scopesStr = intent.getStringExtra(EXTRA_SCOPES);
            if (pkgName != null && scopesStr != null) {
                String[] scopes = scopesStr.split(",");
                activateModuleWithScopes(context, pkgName, scopes);
            }
        }
    }

    private void activateDefaultModule(Context context) {
        String modulePkg = "com.zcg.systemplus";
        String[] defaultScopes = {"system", "com.xtc.i3launcher", "com.xtc.camera.app", "com.xtc.weichat"};
        activateModuleWithScopes(context, modulePkg, defaultScopes);
    }

    private void activateModuleWithScopes(Context context, String modulePkg, String[] scopes) {
        try {
            context.getPackageManager().getPackageInfo(modulePkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            int retries = 0;

            @Override
            public void run() {
                if (!ConfigManager.isBinderAlive()) {
                    if (++retries < 6) {
                        new Handler(Looper.getMainLooper()).postDelayed(this, 500);
                    }
                    return;
                }
                doActivation(modulePkg, scopes);
            }
        }, 500);
    }

    private void doActivation(String modulePkg, String[] scopes) {
        boolean isEnabled = false;
        for (String enabled : ConfigManager.getEnabledModules()) {
            if (enabled.equals(modulePkg)) {
                isEnabled = true;
                break;
            }
        }
        if (!isEnabled) {
            if (!ConfigManager.setModuleEnabled(modulePkg, true)) {
                return;
            }
        }

        var currentScopes = ConfigManager.getModuleScope(modulePkg);
        Set<String> currentPkgSet = new HashSet<>();
        for (ScopeAdapter.ApplicationWithEquals app : currentScopes) {
            currentPkgSet.add(app.packageName);
        }

        Set<String> targetPkgs = new HashSet<>(Arrays.asList(scopes));
        if (currentPkgSet.containsAll(targetPkgs)) {
            return;
        }

        Set<ScopeAdapter.ApplicationWithEquals> newScopes = new HashSet<>(currentScopes);
        for (String pkg : targetPkgs) {
            if (!currentPkgSet.contains(pkg)) {
                newScopes.add(new ScopeAdapter.ApplicationWithEquals(pkg, 0));
            }
        }

        ConfigManager.setModuleScope(modulePkg, false, newScopes);
    }
}