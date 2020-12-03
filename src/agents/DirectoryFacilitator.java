package agents;

import sajas.core.behaviours.Behaviour;
import sajas.core.Agent;
import sajas.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;

public abstract class DirectoryFacilitator extends Agent {
    private boolean isRegistered = false;
    private List<Behaviour> behaviours = new ArrayList<>();

    public DirectoryFacilitator() {
        super();
    }

    @Override
    public void takeDown() {
        if(this.isRegistered) {
            deRegister();
        }
    }

    @Override
    public void addBehaviour(sajas.core.behaviours.Behaviour b) {
        this.behaviours.add(b);
        super.addBehaviour(b);
    }

    @Override
    public void removeBehaviour(Behaviour b) {
        this.behaviours.remove(b);
        super.removeBehaviour(b);
    }

    /**
     * Procedures for the start of the game
     */
    public abstract void init();

    /**
     * Procedures for the end of the game
     */
    public void end() {
        this.getBehaviours().forEach((behaviour) -> {
            behaviour.setAgent(null);
            super.removeBehaviour(behaviour);
        });

        this.getBehaviours().clear();
    }

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

    public List<Behaviour> getBehaviours() {
        return behaviours;
    }

    public void setBehaviours(List<Behaviour> behaviours) {
        this.behaviours = behaviours;
    }
}
