#!/bin/sh

if test ! -n "$TREEX_ROOT"
then
  TREEX_ROOT=`which treex | sed -e 's/bin\/treex$//'`
fi

CURRENT_SCRIPT=`readlink -f $0`
CURRENT_PREFIX=`echo $CURRENT_SCRIPT | sed -e 's/\/[^\/]*$//'`

if test ! -n "$TREEX_ONLINE"
then
  TREEX_ONLINE="$CURRENT_PREFIX/treex_online.pl"
fi

export PERL5LIB=$CURRENT_PREFIX:$PERL5LIB
 
echo "Running perl $TREEX_ONLINE $@"
echo "in directory: $TREEX_ROOT"
echo "PERL5LIB: $PERL5LIB"

cd $TREEX_ROOT
perl $TREEX_ONLINE $@


