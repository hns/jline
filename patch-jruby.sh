#!/bin/bash

if [ -z $JRUBY_HOME ]; then
    JRUBY_HOME=$(dirname $(dirname $(which jruby)))
fi

echo $JRUBY_HOME

sudo cp $JRUBY_HOME/lib/jruby.jar $JRUBY_HOME/lib/jruby.jar.backed-up-by-jline
sudo jar uvf $JRUBY_HOME/lib/jruby.jar -C target/classes jline
