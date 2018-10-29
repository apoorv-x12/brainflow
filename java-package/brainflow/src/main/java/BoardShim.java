import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.lang3.SystemUtils;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class BoardShim {

	public interface DllInterface extends Library {
		DllInterface INSTANCE = SystemUtils.IS_OS_WINDOWS ? (DllInterface) Native.loadLibrary ("BoardController.dll", DllInterface.class) :
			(DllInterface) Native.loadLibrary ("libBoardController.so", DllInterface.class);
		int prepare_session (int board_id, String port_name);
		int start_stream (int buffer_size);
		int stop_stream ();
		int release_session ();
		int get_current_board_data (int num_samples, float [] data_buf, double [] ts_buf, int[] returned_samples);
		int get_board_data_count (int[] result);
		int get_board_data (int data_count, float[] data_buf, double[] ts_buf);
	}
 
	public int num_channels;
	public int board_id;
	public String port_name;
	public BoardShim (int board_id, String port_name) throws BrainFlowError {
		if (board_id == Boards.CYTHON) {
			num_channels = 12;
			this.board_id = board_id;
		}
		else {
			throw new  BrainFlowError ("Wrong board_id", ExitCode.UNSUPPORTED_BOARD_ERROR.get_code ());
		}
		this.port_name = port_name;
	}
	
	public void prepare_session () throws BrainFlowError {
		int ec = DllInterface.INSTANCE.prepare_session (board_id, port_name);
		if (ec != ExitCode.STATUS_OK.get_code ()) {
			throw new BrainFlowError ("Error in prepare_session", ec);
		}
	}
 
	public void start_stream (int buffer_size) throws BrainFlowError {
		int ec = DllInterface.INSTANCE.start_stream (buffer_size);
		if (ec != ExitCode.STATUS_OK.get_code ()) {
			throw new BrainFlowError ("Error in start_stream", ec);
		}
	}
 
	public void stop_stream () throws BrainFlowError {
		int ec = DllInterface.INSTANCE.stop_stream ();
		if (ec != ExitCode.STATUS_OK.get_code ()) {
			throw new BrainFlowError ("Error in stop_stream", ec);
		}
	}

	public void release_session () throws BrainFlowError {
		int ec = DllInterface.INSTANCE.release_session ();
		if (ec != ExitCode.STATUS_OK.get_code ()) {
			throw new BrainFlowError ("Error in release_session", ec);
		}
	}
	
	public int get_board_data_count () throws BrainFlowError {
		int[] res = new int[1];
		int ec = DllInterface.INSTANCE.get_board_data_count (res);
		if (ec != ExitCode.STATUS_OK.get_code ()) {
			throw new BrainFlowError ("Error in get_board_data_count", ec);
		}
		return res[0];
	}
	
	public BoardData get_current_board_data (int num_samples) throws BrainFlowError {
		float[] data_arr = new float[num_samples * num_channels];
		double[] ts_arr = new double[num_samples];
		int[] current_size = new int[1];
		int ec = DllInterface.INSTANCE.get_current_board_data (num_samples, data_arr, ts_arr, current_size);
		if (ec != ExitCode.STATUS_OK.get_code ()) {
			throw new BrainFlowError ("Error in get_current_board_data", ec);
		}
		return new BoardData (num_channels, Arrays.copyOfRange(data_arr, 0, current_size[0] * num_channels),
				Arrays.copyOfRange(ts_arr, 0, current_size[0]));
	}
	
	public BoardData get_immediate_board_data (int num_samples) throws BrainFlowError {
		return get_current_board_data (0);
	}
	
	public BoardData get_board_data () throws BrainFlowError {
		int size = get_board_data_count ();
		float[] data_arr = new float[size * num_channels];
		double[] ts_arr = new double[size];
		int ec = DllInterface.INSTANCE.get_board_data (size, data_arr, ts_arr);
		if (ec != ExitCode.STATUS_OK.get_code ()) {
			throw new BrainFlowError ("Error in get_board_data", ec);
		}
		return new BoardData (num_channels, data_arr, ts_arr);
	}
 
}