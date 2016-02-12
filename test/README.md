Run `build.bat` to compile the scripts. If the remote object classes are
changed, the registry needs to be restarted to unbind the previous version.

Open three command windows and run:

* `rmiregistry`
* `java DA_test_server_main`
* `java DA_test_client_main`