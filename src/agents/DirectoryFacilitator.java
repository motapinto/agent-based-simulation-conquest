package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public abstract class DirectoryFacilitator extends Agent {
    private boolean isRegistered = false;

    public DirectoryFacilitator() {
        super();
    }

    @Override
    public void takeDown() {
        if(this.isRegistered) {
            deRegister();
        }
    }

    /**
     * Procedures for the start of the game
     */
    public abstract void init();

    /**
     * Procedures for the end of the game
     */
    public abstract void end();

    /**
     * Register an agent on the Directory Facilitator(DF)
     * Note: The DF is a centralized registry of entries which associate service descriptions to agent IDs.
     */
    public void registerDF(String service) {
        this.isRegistered = true;
        // Description of the agent that will provide the service
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(this.getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setName(this.getLocalName()); // Sets the name of the described service (normally agent name/local name)
        sd.setType(service); // The service the agent offers
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deregister an agent of the Directory Facilitator(DF)
     */
    public void deRegister() {
        try {
            DFService.deregister(this);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    /**
     * Search service in the Directory Facilitator(DF)
     */
    public DFAgentDescription[] searchDF(String service) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(service);
        dfd.addServices(sd);

        DFAgentDescription[] result = null;
        try {
            result = DFService.search(this, dfd);
        } catch(FIPAException fe) {
            fe.printStackTrace();
        }

        return result;
    }

    public static MessageTemplate getMessageTemplate() {
        return MessageTemplate.and(
            MessageTemplate.not(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE)),
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
            )
        );
    }
}
