cd /data/data/com.moe.shell/files
chown shell:shell moestop
chmod 777 moestop
app_process -Djava.class.path=moestop  /data/local/tmp com.moe.shell.Shell&
