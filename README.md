# BypassABEMARegionCheck
A Xposed Module to Bypass the region check of [ABEMA](https://abema.tv)

# Feature
- Bypass region check
- Bypass root check

# Tested Version
- 10.78.0-2120000
- 100.61.2-2110001 (TV Version)

# Bypass ABEMA Web Region Check
Since ABEMA checks region on its servers, currently it is impossible to bypass the region check by modifying web pages.

For proxy user, Cloudflare Warp now can bypass the region check. You can add a rule for `abema.tv` to divert the network traffic of ABEMA to Cloudflare Warp in the proxy configuration.
