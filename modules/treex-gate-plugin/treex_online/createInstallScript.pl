#!/usr/bin/perl
use strict;
use warnings;
use Module::ExtractUse;

# get a parser
my $p=Module::ExtractUse->new;

open (MYFILE, '>install.cmd');
print MYFILE 'cpan ';
print MYFILE $p->extract_use('treex_online.pl')->string;
close (MYFILE); 



