cd /data/data/com.moe.bgcheck/files
chown shell:shell moestop
chmod 777 moestop
killall app_process
app_process -Djava.class.path=moestop  /data/local/tmp com.moe.shell.Shell&
