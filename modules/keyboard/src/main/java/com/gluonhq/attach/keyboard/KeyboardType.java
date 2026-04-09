/*
 * Copyright (c) 2026, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.attach.keyboard;

/**
 * Defines the type of keyboard to display.
 *
 * <p>On iOS, these map directly to {@code UIKeyboardType} values.
 * On Android, they map to the corresponding {@code InputType} flags.</p>
 *
 * @since 4.0.25
 */
public enum KeyboardType {

    /**
     * The default keyboard, supporting general text input.
     */
    DEFAULT(0),

    /**
     * A keyboard that displays standard ASCII characters.
     */
    ASCII(1),

    /**
     * A keyboard optimized for number and punctuation entry.
     */
    NUMBERS_AND_PUNCTUATION(2),

    /**
     * A keyboard optimized for URL entry (with {@code .}, {@code /},
     * and {@code .com} keys).
     */
    URL(3),

    /**
     * A numeric keypad designed for PIN entry (digits 0-9 only).
     */
    NUMBER_PAD(4),

    /**
     * A keypad designed for entering telephone numbers
     * (digits, {@code *}, and {@code #}).
     */
    PHONE_PAD(5),

    /**
     * A keyboard optimized for entering a person's name or phone number.
     */
    NAME_PHONE_PAD(6),

    /**
     * A keyboard optimized for entering email addresses (with {@code @}
     * and {@code .} keys).
     */
    EMAIL(7),

    /**
     * A numeric keypad with a decimal point.
     */
    DECIMAL_PAD(8),

    /**
     * A keyboard optimized for Twitter text entry
     * (with {@code @} and {@code #} keys).
     */
    TWITTER(9),

    /**
     * A keyboard optimized for web search terms and URL entry.
     */
    WEB_SEARCH(10);

    private final int value;

    KeyboardType(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer value corresponding to this keyboard type.
     *
     * @return the native keyboard type value
     */
    public int getValue() {
        return value;
    }
}

