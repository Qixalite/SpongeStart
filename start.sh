gradle uploadArchives
gradle --stop
cd testplugin; gradle $1 --stacktrace; cd ../;
