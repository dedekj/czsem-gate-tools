package Treex::CzsemScenario;
use Treex::Core;
use Treex::Core::Common;
use Moose;
use Thread::Queue;

extends 'Treex::Core::Scenario';

has blocks_initialized => (
    is            => 'ro',
    isa           => 'Str',
    predicate     => '_blocks_initialized',
    documentation => q(Contains information about initialization of scenario blocks),
);

sub BUILD {
    my $self = shift;
    $self->{'_blocks_initialized'} = 0;
}

sub start {
    my ($self) = @_;

    log_info '@czsem@ Applying process_start';
    
    if (! $self->{'_blocks_initialized'})
    {
      $self->SUPER::start();
      $self->{'_blocks_initialized'} = 1;
    }
    
}




1;