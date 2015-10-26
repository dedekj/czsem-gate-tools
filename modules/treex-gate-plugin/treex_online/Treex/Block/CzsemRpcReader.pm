package Treex::Block::CzsemRpcReader;
use Treex::Core;
use Treex::Core::Common;
use Moose;
use Thread::Queue;

extends 'Treex::Block::Read::BaseReader';

has '+from' => ( default => '' );

has language      => ( isa => 'Treex::Type::LangCode', is => 'ro', required => 1 );



our $dataQueue = new Thread::Queue; 
    
sub next_document {
  my ($self) = @_;
   
  my $docParams = $dataQueue->dequeue;
  
  return if !defined $docParams;
  
  my $doc = $docParams->{doc}; 
  my $text = $docParams->{text}; 
  my $zones = $docParams->{zones}; 

  my $main_zone = $doc->create_zone($self->language, $self->selector);
  $main_zone->set_text($text);
  
  foreach my $zone ( @$zones ) {
    my $bundle = $doc->create_bundle();
    my $cur_zone = $bundle->create_zone( $self->language, $self->selector );

    $cur_zone->set_sentence($zone->{'sentence'});
    
    my $tocs = $zone->{'tokens'};
    
    #This is important, we are not creating an empty atree if there are no tokens. 
    if (@$tocs) {
      
      # create a-tree
      my $a_root = $cur_zone->create_atree();
    
      foreach my $toc ( @$tocs ) {
        # create new a-node
        
        #because some czsem java bug is sometimes sending these id-s... 
        delete $toc->{id};
        delete $toc->{parent_id};
        
        $a_root->create_child($toc);
      }
    }  
  }
  return $doc;
}

1;