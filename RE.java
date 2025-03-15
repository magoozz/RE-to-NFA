package re;

import fa.State;
import fa.nfa.NFA;
import fa.nfa.NFAState;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class RE implements REInterface {

    private String regEx;
    private NFA nfa;
    private NFAState startState;
    private NFAState endState;
    private NFAState branchEnd;
    private int numStates;
    ArrayList<NFAState> allStates = new ArrayList<>();

    public RE(String regEx) {
        this.regEx = regEx;
        this.nfa = new NFA();
        numStates = 0;

        Set<State> statesToAdd = new CopyOnWriteArraySet<>();

        startState = new NFAState("q" + numStates);
        allStates.add(startState);
        numStates++;

        endState = new NFAState("q" + numStates);
        allStates.add(endState);
        numStates++;

        startState.addTransition('e', endState);

        statesToAdd.add(startState);
        statesToAdd.add(endState);
        nfa.addNFAStates(statesToAdd);
        nfa.addStartState(startState.getName());
    }

    @Override
    public NFA getNFA() {
        generateNewNFA(regEx);
        return nfa;
    }

    private void generateNewNFA(String re) {
        char c = re.charAt(0);
        if (c == ')' || c == '*') {
            if (re.length() > 1) {
                generateNewNFA(re.substring(1));
            }
        } else if (openUnionSign(re)) {
            int curNumStates = numStates;
            int indexOfUnion = re.indexOf("|");
            generateNewNFA(re.substring(0, indexOfUnion));
            branchEnd = endState;
            endState = allStates.get(curNumStates);
            generateNewNFA(re.substring(indexOfUnion + 1));
            branchEnd.setNonFinal();
            nfa.addTransition(branchEnd.getName(), 'e', endState.getName());
        } else if (c == '(') {
            String tempName = "q" + numStates;
            int i = 1;
            int count = 1;
            while (count != 0) {
                if (re.charAt(i) == '(') {
                    count++;
                } else if (re.charAt(i) == ')') {
                    count--;
                }
                i++;
            }
            generateNewNFA(re.substring(1, i - 1));
            if (re.length() > i) {
                if (re.charAt(i) == '*') {
//                    System.out.println(nameToState(tempName).getName());
                    nfa.addTransition(endState.getName(), 'e', nameToState(tempName).getName());
                    nfa.addTransition(nameToState(tempName).getName(), 'e', endState.getName());
                }
            }
            generateNewNFA(re.substring(i - 1));
        } else {
            Set<Character> alpha = new CopyOnWriteArraySet<>();
            alpha.add(c);
            nfa.addAbc(alpha);
            Set<State> states = new CopyOnWriteArraySet<>();
            NFAState newStartState = new NFAState("q" + numStates);
            //            System.out.println("q" + numStates);
            allStates.add(newStartState);
            numStates++;
            states.add(newStartState);
            NFAState newEndState = new NFAState("q" + numStates);
            //            System.out.println("q" + numStates);
            newEndState.setFinal();
            allStates.add(newEndState);
            numStates++;
            states.add(newEndState);
            newStartState.addTransition(c, newEndState);
            nfa.addNFAStates(states);
            nfa.addTransition(endState.getName(), 'e', newStartState.getName());
            endState.setNonFinal();
            endState = newEndState;
            if (re.length() > 1) {
                if (re.charAt(1) == '*') {
                    nfa.addTransition(newStartState.getName(), 'e', newEndState.getName());
                    nfa.addTransition(newEndState.getName(), 'e', newStartState.getName());
                    generateNewNFA(re.substring(2));
                } else {
                    generateNewNFA(re.substring(1));
                }
            }

        }
    }

    private NFAState nameToState(String name) {
        for (NFAState state : allStates) {
            if (state.getName().equals(name)) {
                return state;
            }
        }
        return null;
    }

    private boolean openUnionSign(String re) {
        int count = 0;
        for (int i = 0; i < re.length(); i++) {
            if (re.charAt(i) == '(') {
                count++;
            } else if (re.charAt(i) == ')') {
                count--;
            } else if (re.charAt(i) == '|' && count == 0) {
                return true;
            }
        }
        return false;
    }
}
