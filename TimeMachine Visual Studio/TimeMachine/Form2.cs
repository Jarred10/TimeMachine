using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace WindowsFormsApplication1
{
    public partial class Form2 : Form
    {
        public int index { get; set; }

        public Form2()
        {
            InitializeComponent();
            listBox1.DrawMode = System.Windows.Forms.DrawMode.OwnerDrawVariable;
            listBox1.MeasureItem += lst_MeasureItem;
            listBox1.DrawItem += lst_DrawItem;
        }

        private void lst_MeasureItem(object sender, MeasureItemEventArgs e)
        {
            e.ItemHeight = (int)e.Graphics.MeasureString(listBox1.Items[e.Index].ToString(), listBox1.Font, listBox1.Width).Height;
        }

        private void lst_DrawItem(object sender, DrawItemEventArgs e)
        {
            e.DrawBackground();
            e.DrawFocusRectangle();
            e.Graphics.DrawString(listBox1.Items[e.Index].ToString(), e.Font, new SolidBrush(e.ForeColor), e.Bounds);
        }

        private void button1_Click(object sender, EventArgs e)
        {
            if(listBox1.SelectedIndex < 0)
            {
                MessageBox.Show("Please select valid job.");
            }
            else
            {
                index = listBox1.SelectedIndex;
                this.DialogResult = DialogResult.OK;
                this.Close();
            }
        }
    }
}
