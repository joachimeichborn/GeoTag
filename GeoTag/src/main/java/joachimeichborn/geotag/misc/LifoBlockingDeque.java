/*
GeoTag

Copyright (C) 2015  Joachim von Eichborn

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package joachimeichborn.geotag.misc;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class LifoBlockingDeque<E> extends LinkedBlockingDeque<E> {
	private static final long serialVersionUID = 1L;

	/**
	 * This method is equivalent to {@link LinkedBlockingDeque#addFirst}.
	 * 
	 * @return true
	 * @throws IllegalStateException
	 *             if this deque is full
	 * @throws NullPointerException
	 *             if the specified element is null
	 */
	public boolean add(E e) {
		addFirst(e);
		return true;
	}

	/**
	 * This method is equivalent to {@link LinkedBlockingDeque#offerFirst}.
	 * 
	 * @throws NullPointerException
	 *             if the specified element is null
	 */
	public boolean offer(E e) {
		return offerFirst(e);
	}

	/**
	 * This method is equivalent to {@link LinkedBlockingDeque#putFirst}.
	 * 
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 * @throws InterruptedException
	 *             {@inheritDoc}
	 */
	public void put(E e) throws InterruptedException {
		putFirst(e);
	}

	/**
	 * This method is equivalent to {@link LinkedBlockingDeque#offer}.
	 * 
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 * @throws InterruptedException
	 *             {@inheritDoc}
	 */
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return offerFirst(e, timeout, unit);
	}
}
