#!/usr/bin/env perl

use config;

my $main = $config::MAIN_FILE;
my $flag = $config::FLAG;
my $jar = $main;

sub main_build {
	my ($file, $jar) = @_;
    system("kotlinc $file -include-runtime -d $jar");
    print "File '$main' was build to main package '$jar'.\n" if (!$?);
    return $?;
}

$jar =~ s/\.kt$/.jar/;
system("java -jar $jar")
    if (main_build($main, $jar) && $flag eq "--run");
