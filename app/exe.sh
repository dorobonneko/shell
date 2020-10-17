dir=$(dirname $0)
echo $dir
cd /data/local/tmp
rm moestop
if [[ -f /data/local/tmp/moestop ]]
then
app_process -Djava.class.path=moestop  /data/local/tmp com.moe.shell.Shell&
else
cp $dir/moestop /data/local/tmp
chown shell:shell moestop
chmod 777 moestop
app_process -Djava.class.path=moestop  /data/local/tmp com.moe.shell.Shell&
fi
