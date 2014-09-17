package KTH.joel.nodescrabble.Listener;

import KTH.joel.nodescrabble.SocketHandler;

/**
 * Created with IntelliJ IDEA.
 * User: joel
 * Date: 2013-12-26
 * Time: 12:30
 * To change this template use File | Settings | File Templates.
 */
public class MasterListener {
    SocketHandler listener;

    public MasterListener()
    {}

    public MasterListener(SocketHandler main)
    {
        listener = main;
    }
}
